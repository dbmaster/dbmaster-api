package com.branegy.persistence.custom.api;

public interface QueryExpression {
    
    @SuppressWarnings("unchecked")
    default <X> X as(Class<X> clazz) {
        if (getClass().isAssignableFrom(clazz)) {
            return (X) this;
        }
        return null;
    }
}
