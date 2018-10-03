package com.branegy.service.core.exception;

import org.hibernate.exception.ConstraintViolationException;

public class ConstraintViolationApiException extends ApiException {
    private static final long serialVersionUID = -484215747677519794L;
    private final Object entity;

    public ConstraintViolationApiException(String msg, ConstraintViolationException e) {
        super(msg, e);
        this.entity = null;
    }
    
    public ConstraintViolationApiException(String msg, ConstraintViolationApiException e) {
        super(msg, e.getCause());
        this.entity = e.getEntity();
    }

    public ConstraintViolationApiException(ConstraintViolationException e, Object entity) {
        super(e);
        this.entity = entity;
    }

    @Override
    public int getErrorCode() {
        return 503;
    }

    public String getConstraintName(){
        return ((ConstraintViolationException)getCause()).getConstraintName();
    }

    @SuppressWarnings("unchecked")
    public <T> T getEntity() {
        return (T) entity;
    }

}
