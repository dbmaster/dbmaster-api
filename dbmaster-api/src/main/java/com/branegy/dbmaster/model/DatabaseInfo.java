package com.branegy.dbmaster.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DatabaseInfo {
    private final Map<String,Object> customMap = new HashMap<String, Object>();
    private final String name;
    private final String state;
    private final boolean readable;

    public DatabaseInfo(String name, String state, boolean readable) {
        this.name = name;
        this.state = state;
        this.readable = readable;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public boolean isReadable() {
        return readable;
    }

    public <X> void setCustomData(String name, X value) {
        if (value != null) {
            customMap.put(name, value);
        } else {
            customMap.remove(name);
        }
    }

    public void setCustomMap(Map<String, Object> map) {
        customMap.clear();
        customMap.putAll(map);
    }

    public Map<String, Object> getCustomMap() {
        return Collections.unmodifiableMap(customMap);
    }

    @SuppressWarnings("unchecked")
    public <X> X getCustomData(String name) {
        return (X) customMap.get(name);
    }
}