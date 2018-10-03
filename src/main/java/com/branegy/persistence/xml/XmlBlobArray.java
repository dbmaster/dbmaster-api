package com.branegy.persistence.xml;

import java.util.Arrays;

public class XmlBlobArray extends XmlBlobType {

    @Override
    public boolean equals(Object x, Object y){
        return Arrays.deepEquals((Object[])x,(Object[])y);
    }

    @Override
    public int hashCode(Object param){
        return Arrays.deepHashCode((Object[]) param);
    }

}
