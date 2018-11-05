package com.branegy.service.core.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * Initialize lazy associations on managed entities
 *
 * syntax:
 * (<attr-name>(.<attr-name>)+)(,<attr-name>(.<attr-name>)+)*
 * support wildcards: ? and *
 *
 * Model:  * - all fields of Model
 *         *.* - all fields of Model + relation entity level (Model.tables,Model.columns etc)
 *         custom*.*
 *         custom
 */
public class LazyFetchInitializer {
    private final Logger logger = LoggerFactory.getLogger(LazyFetchInitializer.class);
    private final Map<String,ReflectionPath> cache = new WeakHashMap<String, ReflectionPath>();

    // TODO map key support
    // TODO improve error checking
    // TODO replace with FetchProfile
    // TODO Basic lazy fetch?

    private ReflectionPath parse(String fetchPaths, Class<?> clazz, EntityManager em) {
        Metamodel metamodel = em.getMetamodel();
        ManagedType<?> managed = metamodel.managedType(clazz);
        ReflectionPath rootReflector = new ReflectionPathRoot(clazz);
        for (String path:fetchPaths.split(",")){
            if (path.isEmpty()){
                throw new IllegalArgumentException(fetchPaths);
            }
            reflector(metamodel, managed, Arrays.asList(path.split("\\.")), rootReflector);
        }
        logger.debug("Parse '{}' as {}",fetchPaths,rootReflector);
        return rootReflector;
    }

    public <T> void fetch(EntityManager em,String fetchPath,Class<T> clazz,T object){
        String id = clazz.getName()+":"+fetchPath;
        ReflectionPath reflector = cache.get(id);
        if (reflector==null){
            synchronized (cache) {
                reflector = cache.get(id);
                if (reflector==null){
                    reflector = parse(fetchPath, clazz, em);
                    cache.put(id, reflector);
                }
            }
        }
        reflector.initilize(object);
    }

    private void reflector(Metamodel metamodel, ManagedType<?> managed, List<String> paths,
            ReflectionPath reflectorPath) {
        if (paths.isEmpty()){
            return;
        }
        String path = paths.get(0);
        Pattern pattern = Pattern.compile(path.replace("*", "[^\\.]*").replace("?", "[^\\.]"));
        logger.trace("Parse {}, pattern '{}'",paths,pattern);
        for (Attribute<?, ?> attr : getAllAttributes(managed)) {
            String name = attr.getName();
            if (pattern.matcher(name).matches()) {
                logger.trace("Match attribute '{}' with pattern",name);
                ReflectionPath child;
                Member member = attr.getJavaMember();
                switch (attr.getPersistentAttributeType()) {
                case BASIC:
                case EMBEDDED:
                    logger.warn("'"+name + "' is "+attr.getPersistentAttributeType().name()+
                            " binding with EAGER fetch");
                    break;
                case MANY_TO_ONE:
                case ONE_TO_ONE:
                    child = reflectorPath.addChild(getSingleReflectionPath(member));
                    managed = (ManagedType<?>) ((SingularAttribute<?,?>)attr).getType();
                    reflector(metamodel, managed, paths.subList(1, paths.size()), child);
                    break;
                case MANY_TO_MANY:
                case ONE_TO_MANY:
                case ELEMENT_COLLECTION:
                    Type<?> type;
                    if (attr instanceof MapAttribute<?,?,?>){
                        type = ((MapAttribute<?,?,?>)attr).getKeyType();
                        if (type instanceof ManagedType<?>){
                            child = reflectorPath.addChild( getMapKeyReflectionPath(member));
                            managed = (ManagedType<?>) type;
                            reflector(metamodel, managed, paths.subList(1, paths.size()), child);
                            throw new IllegalStateException("map key not support now");
                        }
                        child = getMapValueReflectionPath(member);
                    } else {
                        child = getCollectionReflectionPath(member);
                    }
                    child = reflectorPath.addChild(child);
                    type = ((PluralAttribute<?,?,?>)attr).getElementType();
                    if (type instanceof ManagedType<?>){
                        managed = (ManagedType<?>) type;
                        reflector(metamodel, managed, paths.subList(1, paths.size()), child);
                    }
                    break;
                default:
                    throw new IllegalArgumentException(""+attr.getPersistentAttributeType());
                }
            }
        }
    }

    private ReflectionPath getCollectionReflectionPath(Member member) {
        if (member instanceof Field){
            return new ReflectionPathFieldCollection((Field) member);
        } else {
            return new ReflectionPathMethodCollection((Method)member);
        }
    }

    private ReflectionPath getMapKeyReflectionPath(Member member) {
        if (member instanceof Field){
            return new ReflectionPathFieldKeyMap((Field) member);
        } else {
            return new ReflectionPathMethodKeyMap((Method) member);
        }
    }

    private ReflectionPath getMapValueReflectionPath(Member member) {
        if (member instanceof Field){
            return new ReflectionPathFieldValueMap((Field) member);
        } else {
            return new ReflectionPathMethodValueMap((Method) member);
        }
    }

    private ReflectionPath getSingleReflectionPath(Member member) {
        if (member instanceof Field){
            return new ReflectionPathFieldSingle((Field) member);
        } else {
            return new ReflectionPathMethodSingle((Method) member);
        }
    }

    private List<Attribute<?, ?>> getAllAttributes(ManagedType<?> managed) {
        List<Attribute<?, ?>> attrs = new ArrayList<Attribute<?,?>>();
        while (managed!=null){
            attrs.addAll(managed.getDeclaredAttributes());
            if (managed instanceof IdentifiableType<?>){
                managed = ((IdentifiableType<?>)managed).getSupertype();
            } else {
                managed = null;
            }
        }
        return attrs;
    }

    public static abstract class ReflectionPath{
        protected final Map<ReflectionPath,ReflectionPath> children =
                new LinkedHashMap<ReflectionPath,ReflectionPath>();

        ReflectionPath() {
        }

        public abstract void initilize(Object o);
        protected abstract Object get(Object o);

        protected final void initializeCollection(Object o) {
            if (o==null){
                return;
            }
            Collection<?> collection = (Collection<?>) get(o);
            if (collection==null){
                return;
            }
            if (children.isEmpty()){
                Hibernate.initialize(collection);
            } else {
                for (Object el:collection){
                    for (ReflectionPath child:children.keySet()){
                        child.initilize(el);
                    }
                }
            }
        }

        protected final void initializeValueMap(Object o) {
            if (o==null){
                return;
            }
            Map<?,?> map = (Map<?,?>) get(o);
            if (map==null){
                return;
            }
            if (children.isEmpty()){
                Hibernate.initialize(map);
            } else {
                for (Object el:map.values()){
                    for (ReflectionPath child:children.keySet()){
                        child.initilize(el);
                    }
                }
            }
        }

        protected final void initializeKeyMap(Object o) {
            if (o==null){
                return;
            }
            Map<?,?> map = (Map<?,?>) get(o);
            if (map==null){
                return;
            }
            if (children.isEmpty()){
                Hibernate.initialize(map);
            } else {
                for (Object el:map.keySet()){
                    for (ReflectionPath child:children.keySet()){
                        child.initilize(el);
                    }
                }
            }
        }

        protected final void inititializeSingle(Object o){
            if (o==null){
                return;
            }
            o = get(o);
            if (o instanceof HibernateProxy) {
                o = ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation();
            }
            for (ReflectionPath child:children.keySet()){
                child.initilize(o);
            }
        }

        protected final ReflectionPath addChild(ReflectionPath path){
            ReflectionPath exist = children.get(path);
            if (exist==null){
                children.put(path,path);
                return path;
            } else {
                return exist;
            }
        }

        @Override
        public int hashCode() {
            return children.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof ReflectionPath))
                return false;
            ReflectionPath other = (ReflectionPath) obj;
            if (!children.equals(other.children))
                return false;
            return true;
        }

        protected final String toStringChildren(int offset){
            StringBuilder builder = new StringBuilder();
            for (ReflectionPath child:children.keySet()){
                builder.append('\n');
                builder.append(new String(new char[offset]).replace('\0',' '));
                builder.append("-> ");
                builder.append(child.toString());
                builder.append(child.toStringChildren(offset+3));
            }
            return builder.toString();
        }

        @Override
        public abstract String toString();
    }

    static final class ReflectionPathRoot extends ReflectionPath {
        private final String clazz;

        ReflectionPathRoot(Class<?> clazz) {
            this.clazz = clazz.getSimpleName();
        }

        @Override
        public void initilize(Object o){
            for (ReflectionPath child:children.keySet()){
                child.initilize(o);
            }
        }

        @Override
        protected Object get(Object o) {
            throw new IllegalStateException();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(clazz);
            builder.append(super.toStringChildren(clazz.length()));
            return builder.toString();
        }
    }

    static abstract class ReflectionPathField extends ReflectionPath{
        private final Field field;

        protected ReflectionPathField(Field field) {
            this.field = field;
            this.field.setAccessible(true);
        }

        @Override
        public Object get(Object o){
            try {
                return field.get(o);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String toString() {
            return field.getName()+"(field";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + field.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (!(obj instanceof ReflectionPathField))
                return false;
            ReflectionPathField other = (ReflectionPathField) obj;
            if (!field.equals(other.field))
                return false;
            return true;
        }
    }

    static final class ReflectionPathFieldSingle extends ReflectionPathField{
        public ReflectionPathFieldSingle(Field field) {
            super(field);
        }
        @Override
        public void initilize(Object o) {
            inititializeSingle(o);
        }

        @Override
        public String toString() {
            return super.toString()+")";
        }

    }

    static final class ReflectionPathFieldCollection extends ReflectionPathField{
        public ReflectionPathFieldCollection(Field field) {
            super(field);
        }
        @Override
        public void initilize(Object o) {
            initializeCollection(o);
        }

        @Override
        public String toString() {
            return super.toString()+"/collection)";
        }
    }

    static final class ReflectionPathFieldValueMap extends ReflectionPathField{
        public ReflectionPathFieldValueMap(Field field) {
            super(field);
        }
        @Override
        public void initilize(Object o) {
            initializeValueMap(o);
        }

        @Override
        public String toString() {
            return super.toString()+"/map-value)";
        }


        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj.getClass()==ReflectionPathFieldValueMap.class;
        }
    }

    static final class ReflectionPathFieldKeyMap extends ReflectionPathField{
        public ReflectionPathFieldKeyMap(Field field) {
            super(field);
        }
        @Override
        public void initilize(Object o) {
            initializeKeyMap(o);
        }

        @Override
        public String toString() {
            return super.toString()+"/map-key)";
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj.getClass()==ReflectionPathFieldKeyMap.class;
        }
    }

    static abstract class ReflectionPathMethod extends ReflectionPath{
        private final Method method;

        protected ReflectionPathMethod(Method method) {
            this.method = method;
            this.method.setAccessible(true);
        }

        @Override
        public Object get(Object o){
            try {
                return method.invoke(o);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String toString() {
            return method.getName()+"(method";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + method.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (!(obj instanceof ReflectionPathMethod))
                return false;
            ReflectionPathMethod other = (ReflectionPathMethod) obj;
            if (!method.equals(other.method))
                return false;
            return true;
        }
    }

    static final class ReflectionPathMethodCollection extends ReflectionPathMethod{
        public ReflectionPathMethodCollection(Method method) {
            super(method);
        }
        @Override
        public void initilize(Object o) {
            initializeCollection(o);
        }

        @Override
        public String toString() {
            return super.toString()+"/collection)";
        }
    }

    static final class ReflectionPathMethodValueMap extends ReflectionPathMethod{
        public ReflectionPathMethodValueMap(Method method) {
            super(method);
        }
        @Override
        public void initilize(Object o) {
            initializeValueMap(o);
        }

        @Override
        public String toString() {
            return super.toString()+"/map-value)";
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj.getClass()==ReflectionPathMethodValueMap.class;
        }
    }

    static final class ReflectionPathMethodKeyMap extends ReflectionPathMethod{
        public ReflectionPathMethodKeyMap(Method method) {
            super(method);
        }
        @Override
        public void initilize(Object o) {
            initializeKeyMap(o);
        }

        @Override
        public String toString() {
            return super.toString()+"/map-key)";
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj.getClass()==ReflectionPathMethodKeyMap.class;
        }
    }

    static final class ReflectionPathMethodSingle extends ReflectionPathMethod{
        public ReflectionPathMethodSingle(Method method) {
            super(method);
        }
        @Override
        public void initilize(Object o) {
            inititializeSingle(o);
        }

        @Override
        public String toString() {
            return super.toString()+")";
        }
    }
}