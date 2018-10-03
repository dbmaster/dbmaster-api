package com.branegy.persistence.xml;

import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map.Entry;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;

/**
 * configurations:
 * key=<method-name>.<description>
 * values=<param1>,<param2>
 */
public class XmlBlobType implements UserType,ParameterizedType {
    private static final Logger logger = LoggerFactory.getLogger(XmlBlobType.class);
    private static final int[] TYPES = new int[] { Types.BLOB };
    private final XStream xstream;
    
    public XmlBlobType() {
        xstream = new XStream();
        xstream.addPermission(NoTypePermission.NONE); // TODO add security white list
        xstream.addPermission(AnyTypePermission.ANY); // TODO add security white list
    }

    @Override
    public Object assemble(Serializable cached, Object owner){
        return cached;
    }

    @Override
    public Object deepCopy(Object param){
        if (param==null){
            return null;
        } else {
            try {
                return FastDeepCopy.deepCopy(param);
            } catch (Exception e) {
                throw new HibernateException(e);
            }
        }
    }

    @Override
    public Serializable disassemble(Object value){
        return (Serializable) value;
    }

    /**
     * Deep equals is not supported. Use equals/hashCode override.
     * XmlBlobArray for arrays.
     */
    @Override
    public boolean equals(Object x, Object y){
        return (x == y) || (x != null && y != null && x.equals(y));
    }

    @Override
    public int hashCode(Object param){
        return param.hashCode();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
            SessionImplementor si, Object owner) throws SQLException {
        InputStream reader = rs.getBinaryStream(names[0]);
        try {
            if (reader == null || reader.available()==0) {
                return null;
            } else {
                return xstream.fromXML(new InputStreamReader(reader,Charsets.UTF_8));
            }
        } catch (IOException e) {
            logger.error("",e);
            throw new HibernateException(e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
            SessionImplementor si) throws SQLException {
        if (value != null) {
            byte[] str = xstream.toXML(value).getBytes(Charsets.UTF_8);
            ByteArrayInputStream r = new ByteArrayInputStream(str);
            st.setBinaryStream(index, r, str.length);
        } else {
            st.setNull(index, TYPES[0]);
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner){
        return deepCopy(original);
    }

    @Override
    public Class<?> returnedClass() {
        return Serializable.class;
    }

    @Override
    public int[] sqlTypes() {
        return TYPES;
    }

    @Override
    public void setParameterValues(Properties properties) {
        for (Entry<Object,Object> e:properties.entrySet()){
            configure((String)e.getKey(),(String)e.getValue());
        }
    }

    private void configure(String key,String value){
        String methodName = key.substring(0,key.indexOf('.'));
        String[] values = value.isEmpty()?EMPTY_STRING_ARRAY:value.split(",");
        Method method = findMethod(methodName, values.length);
        if (method==null){
            return;
        }
        Object[] params = convertParameters(values,method.getParameterTypes());
        try {
            method.invoke(xstream, params);
        } catch (Exception e) {
            logger.error("Invoke method {}",method,e);
        }
    }

    private Method findMethod(String methodName,int count){
        Method first = null;
        for (Method method:XStream.class.getMethods()){
            if (method.getName().equals(methodName) && method.getParameterTypes().length==count){
                first = method;
                if (count>0 && (method.getParameterTypes()[0].isPrimitive()
                        || method.getParameterTypes()[0]==String.class)){
                    continue;
                } else {
                    break;
                }
            }
        }
        return first;
    }

    private Object[] convertParameters(String[] params,Class<?>[] parameterTypes){
        Object[] result = new Object[params.length];
        for (int i=0; i<params.length; ++i){
            result[i] = convertParamter(params[i], parameterTypes[i]);
        }
        return result;
    }

    private Object convertParamter(String param,Class<?> parameterType){
        Throwable cause = null;
        if (boolean.class==parameterType){
            return Boolean.parseBoolean(param);
        } else if (int.class==parameterType){
            return Integer.parseInt(param);
        } else if (String.class==parameterType){
            return param;
        } else if (Class.class==parameterType){
            try {
                return Class.forName(param);
            } catch (ClassNotFoundException e) {
                cause = e;
            }
        }
        IllegalArgumentException exception = new IllegalArgumentException("Can't parse value "+param
                +" as "+parameterType);
        if (cause!=null){
            exception.initCause(cause);
        }
        throw exception;
    }

}
