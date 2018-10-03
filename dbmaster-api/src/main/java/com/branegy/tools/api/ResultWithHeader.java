package com.branegy.tools.api;

import java.util.Map;

public final class ResultWithHeader<T> {
    private final Map<String,Object> header;
    private final T value;
    
    public ResultWithHeader(Map<String, Object> header, T value) {
        this.header = header;
        this.value = value;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public T getValue() {
        return value;
    }
}
