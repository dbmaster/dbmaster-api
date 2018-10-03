package com.branegy.persistence.custom;

import java.util.Map.Entry;

abstract class EntryProxy<K, VS, VT> implements Entry<K, VT> {
    private Entry<K, VS> source;

    protected abstract VT toTarget(Object object);

    protected abstract VS toSource(Object object);

    public EntryProxy(Entry<K, VS> source) {
        this.source = source;
    }

    @Override
    public final K getKey() {
        return source.getKey();
    }

    @Override
    public final VT getValue() {
        return toTarget(source.getValue());
    }

    @Override
    public final VT setValue(VT paramV) {
        return toTarget(source.setValue(toSource(paramV)));
    }

    @Override
    public final boolean equals(Object paramObject) {
        if (!(paramObject instanceof Entry<?, ?>))
            return false;
        Entry<?, ?> localEntry = (Entry<?, ?>) paramObject;
        Object obj1 = getKey();
        Object obj2 = localEntry.getKey();
        if ((obj1 == obj2) || ((obj1 != null) && (obj1.equals(obj2)))) {
            Object localObject3 = getValue();
            Object localObject4 = localEntry.getValue();
            if ((localObject3 == localObject4)
                    || ((localObject3 != null) && (localObject3.equals(localObject4))))
                return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return source.hashCode();
    }

    @Override
    public final String toString() {
        return getKey() + "=" + getValue();
    }

}
