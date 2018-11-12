package com.branegy.dbmaster.model;

import static com.branegy.dbmaster.model.ModelObject.updateCollectionOrder;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class ColumnListProxy<E extends Column> implements List<E> {
    private final List<E> source;
    private final ModelObject parent;

    ColumnListProxy(List<E> source, ModelObject parent) {
        this.source = source;
        this.parent = parent;
    }

    public int size() {
        return source.size();
    }

    public boolean isEmpty() {
        return source.isEmpty();
    }

    public boolean contains(Object o) {
        return source.contains(o);
    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            final Iterator<E> it = source.iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public E next() {
                return it.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                return it.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return it.equals(obj);
            }
        };
    }

    public Object[] toArray() {
        return source.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return source.toArray(a);
    }

    public boolean add(E e) {
        boolean add = source.add(e);
        if (add){
            onAdd(e);
        }
        return add;
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        boolean removed = source.remove(o);
        if (removed){
            onRemove((E) o);
        }
        return removed;
    }

    public boolean containsAll(Collection<?> c) {
        return source.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }
  
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
        return source.equals(o);
    }

    public int hashCode() {
        return source.hashCode();
    }

    public E get(int index) {
        return source.get(index);
    }

    public E set(int index, E element) {
        E set = source.set(index, element);
        onRemove(set);
        onAdd(element);
        return set;
    }

    public void add(int index, E element) {
        source.add(index, element);
        onAdd(element);
    }

    public E remove(int index) {
        E remove = source.remove(index);
        onRemove(remove);
        return remove;
    }

    public int indexOf(Object o) {
        return source.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return source.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return new ListIterator<E>() {
            final ListIterator<E> it = source.listIterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public E next() {
                return it.next();
            }

            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            public E previous() {
                return it.previous();
            }

            public int nextIndex() {
                return it.nextIndex();
            }

            public int previousIndex() {
                return it.previousIndex();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            public void add(E e) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public int hashCode() {
                return it.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return it.equals(obj);
            }
        };
    }

    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {
            final ListIterator<E> it = source.listIterator(index);

            public boolean hasNext() {
                return it.hasNext();
            }

            public E next() {
                return it.next();
            }

            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            public E previous() {
                return it.previous();
            }

            public int nextIndex() {
                return it.nextIndex();
            }

            public int previousIndex() {
                return it.previousIndex();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            public void add(E e) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public int hashCode() {
                return it.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return it.equals(obj);
            }
        };
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new ColumnListProxy<E>(source.subList(fromIndex, toIndex), parent);
    }
    
    protected void onRemove(E o){
        o.setParent(null);
    }
    
    protected void onAdd(E e){
        e.setParent(parent);
        updateCollectionOrder(source);
    }
    
    protected void onAddAll() {
        for (E e:source){
            e.setParent(parent);
        }
        updateCollectionOrder(source);
    }

}
