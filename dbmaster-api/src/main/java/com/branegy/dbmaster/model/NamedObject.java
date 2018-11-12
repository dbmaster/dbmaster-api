package com.branegy.dbmaster.model;

import java.util.HashMap;
import java.util.Map;

public class NamedObject {
    
    public String name;
    public String type;
    
    public final Map<String, Object> data;
    
    public NamedObject(String name, String type) {
        this.name = name;
        this.type = type;
        data = new HashMap<String, Object>();
    }

}
