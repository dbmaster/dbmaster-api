package com.branegy.scripting.api;

import javax.script.ScriptException;

import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class ScriptApiException extends ApiException {

    public ScriptApiException(ScriptException e) {
        super(e);
    }

    @Override
    public synchronized ScriptException getCause() {
        return (ScriptException) super.getCause();
    }
    
    @Override
    public String getMessage() {
        return getCause().getMessage();
    }
}
