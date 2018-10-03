package com.branegy.persistence.custom;

import java.util.Iterator;

abstract class IteratorProxy<S, T> implements Iterator<T> {
    private Iterator<S> source;

    public IteratorProxy(Iterator<S> source) {
        this.source = source;
    }

    protected abstract T toTarget(S source);

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public T next() {
        return toTarget(source.next());
    }

    @Override
    public void remove() {
        source.remove();
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

}
