package com.branegy.service.core.exception;

public class AuthorizationApiException extends ApiException {
    private static final long serialVersionUID = 2807399599773648775L;

    public AuthorizationApiException() {
    }

    public AuthorizationApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationApiException(String s) {
        super(s);
    }

    public AuthorizationApiException(Throwable cause) {
        super(cause);
    }

    @Override
    public int getErrorCode() {
        return 201;
    }

}
