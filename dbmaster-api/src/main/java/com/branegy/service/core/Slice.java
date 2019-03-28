package com.branegy.service.core;

import java.util.ArrayList;
import java.util.List;

import com.branegy.persistence.util.SliceList;


// TODO (Slava) Describe when Slice should be used instead of List
public interface Slice<E> extends List<E> {
    int getOffset();
    int getTotalSize();
    
    public static <E> Slice<E> subSlice(List<E> source, int fromIndex, int toIndex){
        fromIndex = Math.min(fromIndex,source.size());
        toIndex = Math.min(toIndex, source.size());
        return new SliceList<>(source.subList(fromIndex,toIndex), fromIndex, source.size());
    }
    
    public static <E> Slice<E> copyOf(List<E> source, int fromIndex, int toIndex){
        fromIndex = Math.min(fromIndex,source.size());
        toIndex = Math.min(toIndex, source.size());
        return new SliceList<>(new ArrayList<>(source.subList(fromIndex,toIndex)), fromIndex, source.size());
    }
}
