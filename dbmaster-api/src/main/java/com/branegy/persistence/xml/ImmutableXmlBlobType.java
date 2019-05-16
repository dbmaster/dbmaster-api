package com.branegy.persistence.xml;

public class ImmutableXmlBlobType extends XmlBlobType{
    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object deepCopy(Object param) {
        return param;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }
}
