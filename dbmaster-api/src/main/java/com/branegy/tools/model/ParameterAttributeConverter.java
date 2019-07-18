package com.branegy.tools.model;

import java.util.Collections;
import java.util.Map;

import javax.persistence.AttributeConverter;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;

public class ParameterAttributeConverter implements AttributeConverter<Map<String, Object>, byte[]> {
    protected final XStream xstream = new XStream();
    
    @Override
    public byte[] convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute != null && !attribute.isEmpty()) {
            return xstream.toXML(attribute).getBytes(Charsets.UTF_8);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length==0) {
            return Collections.emptyMap();
        } else {
            return (Map<String, Object>) xstream.fromXML(new String(dbData, Charsets.UTF_8));
        }
    }

}
