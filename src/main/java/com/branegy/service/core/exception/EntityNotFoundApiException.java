package com.branegy.service.core.exception;

import java.util.Arrays;

public class EntityNotFoundApiException extends ApiException {
    private static final long serialVersionUID = 1421414692142287773L;
    private final Class<?> entityClazz;
    private final Object entityId;

    public EntityNotFoundApiException(Class<?> entityClazz, Object entityId) {
        super(String.format("Entity %s/%s not found",entityClazz.getName(),
                entityId instanceof Object[]?Arrays.deepToString((Object[]) entityId):entityId));
        this.entityClazz = entityClazz;
        this.entityId = entityId;
    }

    public EntityNotFoundApiException(Throwable t,Class<?> entityClazz, Object entityId) {
        super(t);
        this.entityClazz = entityClazz;
        this.entityId = entityId;
    }

    @Override
    public int getErrorCode() {
        return 100;
    }

    public Class<?> getEntityClazz() {
        return entityClazz;
    }

    public Object getEntityId() {
        return entityId;
    }



}
