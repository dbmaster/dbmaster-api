package com.branegy.persistence.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.branegy.service.core.Slice;

public final class SliceList<E> implements Slice<E> {
    private final List<E> source;
    private final int offset;
    private final int totalSize;

    public SliceList(List<E> source,int offset,int totalSize){
        this.source = source;
        this.offset = offset;
        this.totalSize = totalSize;
    }

    public SliceList(List<E> source){
        this.source = source;
        this.offset = 0;
        this.totalSize = source.size();
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public boolean contains(Object obj) {
        return source.contains(obj);
    }

    @Override
    public Iterator<E> iterator() {
        return source.iterator();
    }

    @Override
    public Object[] toArray() {
        return source.toArray();
    }

    @Override
    public <T> T[] toArray(T[] clazz) {
        return source.toArray(clazz);
    }

    @Override
    public boolean add(E el) {
        return source.add(el);
    }

    @Override
    public boolean remove(Object obj) {
        return source.remove(obj);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return source.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return source.addAll(collection);
    }

    @Override
    public boolean addAll(int paramInt, Collection<? extends E> collection) {
        return source.addAll(paramInt, collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return source.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return source.retainAll(collection);
    }

    @Override
    public void clear() {
        source.clear();
    }

    @Override
    public E get(int paramInt) {
        return source.get(paramInt);
    }

    @Override
    public E set(int paramInt, E paramE) {
        return source.set(paramInt,paramE);
    }

    @Override
    public void add(int paramInt, E paramE) {
        source.add(paramInt, paramE);
    }

    @Override
    public E remove(int paramInt) {
        return source.remove(paramInt);
    }

    @Override
    public int indexOf(Object paramObject) {
        return source.indexOf(paramObject);
    }

    @Override
    public int lastIndexOf(Object paramObject) {
        return source.lastIndexOf(paramObject);
    }

    @Override
    public ListIterator<E> listIterator() {
        return source.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int paramInt) {
        return source.listIterator(paramInt);
    }

    @Override
    public List<E> subList(int paramInt1, int paramInt2) {
        return source.subList(paramInt1, paramInt2);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public String toString() {
        return String.format("[%d..%d] of [0..%d], %s",offset,offset+size(),totalSize,source.toString());
    }

}
