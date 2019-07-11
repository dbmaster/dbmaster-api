package com.branegy.persistence.custom.api;

public interface QueryExpression {
    
    @SuppressWarnings("unchecked")
    default <X> X as(Class<X> clazz) {
        if (clazz!=null && clazz.isAssignableFrom(getClass())) {
            return (X) this;
        }
        return null;
    }
}
