package com.branegy.dbmaster.sync.impl;

import java.util.ArrayList;
import java.util.Collection;

// TODO anybody's using this class?
public class RootObject {
    public final static String TYPE= "RootObjectType";
    
    private String name;
    
    private String type;

    public RootObject(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Collection<Object> childs = new ArrayList<Object>();

}