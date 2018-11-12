package com.branegy.dbmaster.connection;


@SuppressWarnings("serial")
public class DriverConnectionApiException extends ConnectionApiException {
   
    public DriverConnectionApiException(String message,Exception e) {
        super(message, e);
    }
}
