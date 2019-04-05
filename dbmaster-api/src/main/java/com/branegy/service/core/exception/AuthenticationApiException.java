package com.branegy.service.core.exception;

import java.util.Arrays;

import com.branegy.dbmaster.core.Permission.Role;


public class AuthenticationApiException extends ApiException {
    private static final long serialVersionUID = -813221821178538010L;

    private boolean create;

    public AuthenticationApiException() {
    }
    
    public AuthenticationApiException(Role... roles) {
        this("required: "+ Arrays.asList(roles));
    }

    public AuthenticationApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationApiException(String s) {
        super(s);
    }

    public AuthenticationApiException(Throwable cause) {
        super(cause);
    }

    @Override
    public int getErrorCode() {
        return 200;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }
}
