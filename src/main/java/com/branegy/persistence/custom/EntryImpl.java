package com.branegy.persistence.custom;

import java.util.Map;
import java.util.Map.Entry;

final class EntryImpl<K, V> implements Entry<K, V> {
    private final K key;
    private V value;

    public EntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V setValue(V paramV) {
        Object localObject = this.value;
        this.value = paramV;
        return (V) localObject;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (!(paramObject instanceof Map.Entry<?, ?>))
            return false;
        Map.Entry<?, ?> localEntry = (Map.Entry<?, ?>) paramObject;
        Object key = getKey();
        Object value = localEntry.getKey();
        if ((key == value) || ((key != null) && (key.equals(value)))) {
            Object key2 = getValue();
            Object val2 = localEntry.getValue();
            if ((key2 == val2)
                    || ((key2 != null) && (key2.equals(val2))))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (((this.key == null) ? 0 : this.key.hashCode()) ^ ((this.value == null) ? 0 : this.value
                .hashCode()));
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }

}
