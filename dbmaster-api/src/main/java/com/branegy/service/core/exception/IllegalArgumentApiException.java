package com.branegy.service.core.exception;

public class IllegalArgumentApiException extends ApiException {
    private static final long serialVersionUID = 8371645580093943027L;

    public IllegalArgumentApiException(Throwable t) {
        super("Parameter invalid",t);
    }

    public IllegalArgumentApiException(Throwable throwable, String message) {
        super(message, throwable);
    }

    public IllegalArgumentApiException() {
        super(String.format("Parameter invalid"));
    }

    public IllegalArgumentApiException(String msg) {
        super(msg);
    }

    @Override
    public int getErrorCode() {
        return 500;
    }

}
