package com.branegy.tools.model;

import javax.persistence.AttributeConverter;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;

public class ToolConfigAttributeConvertor implements AttributeConverter<ToolConfig, byte[]> {
    protected final XStream xstream;

    public ToolConfigAttributeConvertor() {
        xstream = new XStream();
        xstream.alias("tool", ToolConfig.class);
        xstream.alias("parameter", Parameter.class);
        xstream.alias("output", OutputEngine.class);
        xstream.addImplicitCollection(ToolConfig.class, "outputEngines");
        xstream.useAttributeFor(ToolConfig.class,"id");
        xstream.aliasAttribute(ToolConfig.class, "parentId","parent-tool");
        xstream.useAttributeFor(ToolConfig.class,"title");
        xstream.useAttributeFor(ToolConfig.class,"hidden");
        xstream.useAttributeFor(ToolConfig.class,"projectTypes");
        
        xstream.useAttributeFor(Parameter.class,"name");
        xstream.useAttributeFor(Parameter.class,"suggestion");
        xstream.useAttributeFor(Parameter.class,"title");
        xstream.useAttributeFor(Parameter.class,"type");
        xstream.useAttributeFor(Parameter.class,"hidden");
        xstream.useAttributeFor(Parameter.class,"multiple");
        xstream.useAttributeFor(Parameter.class,"required");
        xstream.useAttributeFor(Parameter.class,"defaultValue");
        xstream.useAttributeFor(Parameter.class,"disableSuggestionCache");
        xstream.useAttributeFor(Parameter.class,"width");
        xstream.useAttributeFor(Parameter.class,"height");
    }

    @Override
    public byte[] convertToDatabaseColumn(ToolConfig attribute) {
        if (attribute != null) {
            return xstream.toXML(attribute).getBytes(Charsets.UTF_8);
        } else {
            return null;
        }
    }

    @Override
    public ToolConfig convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length==0) {
            return null;
        } else {
            return (ToolConfig) xstream.fromXML(new String(dbData, Charsets.UTF_8));
        }
    }

}
