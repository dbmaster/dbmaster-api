package com.branegy.service.core.exception;

public class EntityExistApiException extends ApiException {
    private static final long serialVersionUID = -3345132815999098222L;
    private final Class<?> entityClazz;
    private final Object entityId;

    public EntityExistApiException() {
        this(null,null);
    }

    public EntityExistApiException(Class<?> entityClazz, Object entityId) {
        this.entityClazz = entityClazz;
        this.entityId = entityId;
    }
    
    public EntityExistApiException(String msg, Class<?> entityClazz, Object entityId) {
        super(msg);
        this.entityClazz = entityClazz;
        this.entityId = entityId;
    }

    @Override
    public int getErrorCode() {
        return 504;
    }

    public Class<?> getEntityClazz() {
        return entityClazz;
    }

    public Object getEntityId() {
        return entityId;
    }

}
