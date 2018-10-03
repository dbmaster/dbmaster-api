package com.branegy.tools.api;

import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class ToolIllegalOutputFormatException extends ApiException {

    public ToolIllegalOutputFormatException(String message) {
        super(message);
    }

}
