package com.branegy.persistence.custom;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author keygen
 *
 * @param <S>
 *            - source collection
 * @param <T>
 *            - target interface
 */
abstract class CollectionProxy<S, T> implements Collection<T> {
    private Collection<S> source;

    public CollectionProxy(Collection<S> source) {
        this.source = source;
    }

    protected abstract T toTarget(Object object);

    protected abstract S toSource(Object object);

    private List<S> covertToSourceCollection(Collection<?> values) {
        List<S> result = new ArrayList<S>(values.size());
        for (Object o : values) {
            result.add(toSource(o));
        }
        return result;
    }

    @Override
    public boolean add(T target) {
        return source.add(toSource(target));
    }

    @Override
    public boolean addAll(Collection<? extends T> paramCollection) {
        return source.addAll(covertToSourceCollection(paramCollection));
    }

    @Override
    public void clear() {
        source.clear();
    }

    @Override
    public boolean contains(Object target) {
        return source.contains(toSource(target));
    }

    @Override
    public boolean containsAll(Collection<?> paramCollection) {
        return source.containsAll(covertToSourceCollection(paramCollection));
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorProxy<S, T>(source.iterator()) {
            @Override
            protected T toTarget(S source) {
                return CollectionProxy.this.toTarget(source);
            }
        };
    }

    @Override
    public boolean remove(Object target) {
        return source.remove(toSource(target));
    }

    @Override
    public boolean removeAll(Collection<?> paramCollection) {
        return source.removeAll(covertToSourceCollection(paramCollection));
    }

    @Override
    public boolean retainAll(Collection<?> paramCollection) {
        return source.retainAll(covertToSourceCollection(paramCollection));
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public Object[] toArray() {
        Object[] values = source.toArray();
        Object[] target = new Object[values.length];
        for (int i = 0; i < values.length; ++i) {
            target[i] = toTarget(values[i]);
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(E[] clazz) {
        Object[] values = source.toArray();
        E[] result = (E[]) Array.newInstance(clazz.getClass(), values.length);
        for (int i = 0; i < values.length; ++i) {
            result[i] = (E) toTarget(values[i]);
        }
        return result;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Collection<?>))
            return false;
        Iterator<S> it1 = source.iterator();
        Iterator<?> it2 = ((Collection<?>) paramObject).iterator();
        while ((it1.hasNext()) && (it2.hasNext())) {
            Object obj1 = toTarget(it1.next());
            Object obj2 = it2.next();
            if (obj1 == null) {
                if (obj2 != null) {
                    return false;
                }
            } else if (!(obj1.equals(obj2))) {
                return false;
            }
        }
        return ((!(it1.hasNext())) && (!(it2.hasNext())));
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public String toString() {
        Iterator<S> localIterator = source.iterator();
        if (!(localIterator.hasNext()))
            return "[]";
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append('[');
        while (true) {
            S localObject = localIterator.next();
            strBuilder.append((localObject == source) ? "(this Collection)" : toTarget(localObject));
            if (!(localIterator.hasNext())) {
                return strBuilder.append(']').toString();
            }
            strBuilder.append(", ");
        }
    }

}
