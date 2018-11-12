package com.branegy.dbmaster.connection;

import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class ConnectionApiException extends ApiException {

    public ConnectionApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConnectionApiException(String message) {
        super(message);
    }
}