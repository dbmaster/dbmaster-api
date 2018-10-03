package com.branegy.service.core;

import java.util.List;


// TODO (Slava) Describe when Slice should be used instead of List
public interface Slice<E> extends List<E> {
    int getOffset();
    int getTotalSize();
}
