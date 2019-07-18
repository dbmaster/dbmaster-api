package com.branegy.service.connection.model;

import javax.persistence.AttributeConverter;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;

import com.branegy.service.connection.model.DatabaseConnection.PropertyInfo;

public class PropertyInfoAttributeConverter implements AttributeConverter<PropertyInfo[], byte[]> {
    protected final PropertyInfo[] EMPTY_PROPERTY_INFO = new PropertyInfo[0]; 
    protected final XStream xstream;
    
    public PropertyInfoAttributeConverter() {
        xstream = new XStream();
        xstream.alias("property", DatabaseConnection.PropertyInfo.class);
    }

    @Override
    public byte[] convertToDatabaseColumn(PropertyInfo[] attribute) {
        if (attribute != null && attribute.length!=0) {
            return xstream.toXML(attribute).getBytes(Charsets.UTF_8);
        } else {
            return null;
        }
    }

    @Override
    public PropertyInfo[] convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length==0) {
            return EMPTY_PROPERTY_INFO;
        } else {
            return (PropertyInfo[]) xstream.fromXML(new String(dbData, Charsets.UTF_8));
        }
    }

}
