package com.branegy.service.core.exception;

public class EntityNotUniqueResultApiException extends ApiException {
    private static final long serialVersionUID = -5067358058867269126L;

    public EntityNotUniqueResultApiException(EntityNotUniqueResultApiException e) {
        super(e);
    }

    @Override
    public int getErrorCode() {
        return 509;
    }

}
