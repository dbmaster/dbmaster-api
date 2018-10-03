package com.branegy.service.core.exception;

@SuppressWarnings("serial")
public class EntityVersionMismatchApiException extends ApiException {
    private final Object existEntity;

    @Override
    public int getErrorCode() {
        return 0;
    }

    public EntityVersionMismatchApiException(Object existEntity) {
        this.existEntity = existEntity;
    }

    /*public EntityVersionMismatchApiException() {
        this.existEntity = null;
    }

    public EntityVersionMismatchApiException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
        this.existEntity = null;
    }

    public EntityVersionMismatchApiException(String paramString) {
        super(paramString);
        this.existEntity = null;
    }

    public EntityVersionMismatchApiException(Throwable paramThrowable) {
        super(paramThrowable);
        this.existEntity = null;
    }*/

    public EntityVersionMismatchApiException(Throwable paramThrowable,Object existEntity) {
        super(paramThrowable);
        this.existEntity = existEntity;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExistEntity() {
        return (T) existEntity;
    }
}
