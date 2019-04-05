package com.branegy.service.core.exception;

public class DisabledUserApiException extends AuthorizationApiException {
    private static final long serialVersionUID = 4885210463688130731L;
    private final long userId;
    
    public DisabledUserApiException(long userId) {
        this.userId = userId;
    }

    @Override
    public int getErrorCode() {
        return 202;
    }

    public long getUserId() {
        return userId;
    }
}
