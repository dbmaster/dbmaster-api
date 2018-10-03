package com.branegy.service.core.exception;

public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 690931269458002903L;

    protected int errorCode = 0;

    protected ApiException() {}

    public ApiException(Throwable throwable) {
        super(throwable);
        assert !(throwable instanceof ApiException);
    }

    public ApiException(String message, Throwable throwable) {
        super(message, throwable);
        assert !(throwable instanceof ApiException);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
