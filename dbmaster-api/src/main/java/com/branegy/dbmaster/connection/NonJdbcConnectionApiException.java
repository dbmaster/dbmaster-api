package com.branegy.dbmaster.connection;

@SuppressWarnings("serial")
public class NonJdbcConnectionApiException extends ConnectionApiException {
    public NonJdbcConnectionApiException(String message) {
        super(message);
    }

}
