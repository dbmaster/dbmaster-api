package com.branegy.dbmaster.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A special implementation of map that accepts non-null string for keys and ignores key case.
 */
public class NameMap<V> extends LinkedHashMap<String, V> {
    private static final long serialVersionUID = -3026936733917800245L;

    public NameMap(int size) {
        super(size);
    }

    public NameMap() {
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return super.containsKey(((String)key).toLowerCase());
        } else {
            return false;
        }
    }

    protected void ensureStringArgument(Object key) {
        if (key==null) {
            throw new NullPointerException();
        } else if (!(key instanceof String)){
            throw new IllegalArgumentException(key.getClass().getName());
        }
    }

    @Override
    public V get(Object key) {
        ensureStringArgument(key);
        return super.get(((String)key).toLowerCase());
    }

    @Override
    public V put(String key, V value) {
        ensureStringArgument(key);
        return super.put(((String)key).toLowerCase(), value);
    }

    @Override
    public V remove(Object key) {
        ensureStringArgument(key);
        return super.remove(((String)key).toLowerCase());
    }

    public List<V> toList() {
        return new ArrayList<V>(values());
    }
}
