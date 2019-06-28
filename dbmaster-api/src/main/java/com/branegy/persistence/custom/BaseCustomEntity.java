package com.branegy.persistence.custom;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.branegy.persistence.BaseEntity;

@MappedSuperclass
public abstract class BaseCustomEntity extends BaseEntity {
    public static final String CUSTOMFIELD_VALUE_TABLE  = "CUSTOMFIELD_VALUE";

    @Transient
    private SortedMap<EmbeddableKey,EmbeddablePrimitiveContainer> map;
   
    @Transient
    private Map<String,Object> viewMap;
    
    
    protected final SortedMap<EmbeddableKey,EmbeddablePrimitiveContainer> getInnerCustomMap(){
        return this.map;
    }
    protected abstract SortedMap<EmbeddableKey,EmbeddablePrimitiveContainer> getMap();
   
    // hibernate setter
    @SuppressWarnings("unused")
    private void setMap(SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> map) {
        this.map = map;
    }
   
    
    public final boolean hasCustomProperties() {
        return !map.isEmpty();
    }

    public final void forEachCustomData(BiConsumer<String, Object> consumer) {
        if (hasCustomProperties()) {
            getCustomMap().forEach(consumer);
        }
    }

    public final void setCustomMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            getCustomMap().clear();
        } else {
            Map<String, Object> m = getCustomMap();
            m.clear();
            m.putAll(map);
        }
    }

    public final String getDiscriminator() {
        return getDiscriminator(getClass());
    }
    
    public static final String getDiscriminator(Class<? extends BaseCustomEntity> clazz){
        if (clazz == null) {
            throw new IllegalStateException("Clazz cannot be null");
        } else if (clazz == BaseCustomEntity.class){
            throw new IllegalArgumentException("Clazz should extend BaseCustomEntity");
        } else if (clazz.isAnnotationPresent(CustomFieldDiscriminator.class)) {
            CustomFieldDiscriminator annotation = clazz.getAnnotation(CustomFieldDiscriminator.class);
            assert annotation.value().length() > 0;
            return annotation.value();
        } else {
            throw new IllegalStateException("Add @" + CustomFieldDiscriminator.class.getSimpleName()
                    + " annotation to " + clazz.getName());
        }
    }

    @Override
    public String toString() {
        return super.toString()+ (map!=null?" "+map:"");
    }
    
    private SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getSubMapByKey(String key) {
        return map.subMap(
                new EmbeddableKey(key, 0, null),
                new EmbeddableKey(key, Integer.MAX_VALUE, null));
    }
    
    @SuppressWarnings("unchecked")
    private void initViewMap() {
        if (viewMap == null) {
            if (map == null) {
                map = new TreeMap<>();
            }
            Map<String, Object> innerMap = new HashMap<String, Object>();
            String prevKey = null;
            int count = 0;
            for (Entry<EmbeddableKey, EmbeddablePrimitiveContainer> e:map.entrySet()) {
                EmbeddableKey ekey = e.getKey();
                String key = ekey.getFieldName();
                if (prevKey!=null && !prevKey.equals(key)) {
                    if (count>1) {
                        innerMap.compute(prevKey, (k,v)->Collections.unmodifiableList((List<Object>) v));
                    }
                    count = 0;
                }
                
                Object object = e.getValue().getObject();
                innerMap.compute(key, (k,v)->{
                    if (v == null) {
                        return object;
                    } else {
                        List<Object> result = new ArrayList<>(); // change
                        result.add(v);
                        result.add(object);
                        return result;
                    }
                });
                prevKey = key;
                count++;
            }
            if (prevKey!=null) {
                if (count>1) {
                    innerMap.compute(prevKey, (k,v)->Collections.unmodifiableList((List<Object>) v));
                }
            }
            viewMap = new AbstractMap<String, Object>() {
                @Override
                public Set<Entry<String, Object>> entrySet() {
                    return innerMap.entrySet();
                }

                @Override
                public int size() {
                    return innerMap.size();
                }

                @Override
                public boolean isEmpty() {
                    return map.isEmpty();
                }

                @Override
                public boolean containsValue(Object value) {
                    return innerMap.containsValue(value);
                }

                @Override
                public boolean containsKey(Object key) {
                    return innerMap.containsKey(key);
                }

                @Override
                public Object get(Object key) {
                    return innerMap.get(key);
                }
                
                // TODO recalculate indexes!
                // TODO on arrays changes 
                private Object merge(
                        String key,
                        Iterator<Object> valuesIt, 
                        Iterator<Entry<EmbeddableKey, EmbeddablePrimitiveContainer> > containerIt) {
                    int lastIndex = 0;
                    int count = 0;
                    Object result = null;
                    while(valuesIt.hasNext() && containerIt.hasNext()) {
                        Entry<EmbeddableKey, EmbeddablePrimitiveContainer> next = containerIt.next();
                        Object next2 = valuesIt.next();
                        next.getValue().setObject(next2);
                        lastIndex = next.getKey().getValueOrder()+1;
                        if (count == 0) {
                            result = next2;
                        } else if (count == 1) {
                            List<Object> resultList = new ArrayList<>(); 
                            resultList.add(count);
                            resultList.add(next2);
                            result = resultList;
                        } else {
                            ((List<Object>)result).add(next2);
                        }
                        count++;
                    }
                    while (valuesIt.hasNext()) {
                        Object next2 = valuesIt.next();
                        map.put(new EmbeddableKey(key, lastIndex++, getDiscriminator()),
                                new EmbeddablePrimitiveContainer(next2));
                        if (count == 0) {
                            result = next2;
                        } else if (count == 1) {
                            List<Object> resultList = new ArrayList<>(); // change
                            resultList.add(count);
                            resultList.add(next2);
                            result = resultList;
                        } else {
                            ((List<Object>)result).add(next2);
                        }
                        count++;
                    }
                    if (count>1) {
                        result = Collections.unmodifiableList((List<Object>) result);
                    }
                    while (containerIt.hasNext()) {
                        containerIt.remove();
                        containerIt.next();
                    }
                    return innerMap.put(key, result);
                }
                

                @Override
                public Object put(String key, Object value) {
                    if (value == null) {
                        return remove(key);
                    }
                    return innerMap.compute(key, (k,v)->{
                        Iterator<Object> valuesIt;
                        if (value instanceof Collection<?>) {
                            valuesIt = ((Collection<Object>)value).iterator();
                        } else {
                            valuesIt = Collections.singleton(value).iterator();
                        }
                        Iterator<Entry<EmbeddableKey, EmbeddablePrimitiveContainer>> containerIt;
                        if (v == null) { // no previous, simple add
                            containerIt = Collections.emptyIterator();
                        } else { // collection | simple value
                            containerIt = getSubMapByKey(key).entrySet().iterator();
                        }
                        return merge(key, valuesIt, containerIt);
                    });
                }

                @Override
                public Object remove(Object key) {
                    Object remove = innerMap.remove(key);
                    if (remove!=null) {
                        getSubMapByKey(key.toString()).clear();
                    }
                    return remove;
                }

                @Override
                public void clear() {
                    map.clear();
                    innerMap.clear();
                }
            };
        }
    }
    
    @SuppressWarnings("unchecked")
    public <X> X getCustomData(String key) {
        initViewMap();
        return (X) viewMap.get(key);
    }
    
    public <X> void setCustomData(String key, X value) {
        initViewMap();
        viewMap.put(key, value);
    }
    
    public final Map<String,Object> getCustomMap(){
        initViewMap();
        return viewMap;
    }
}
