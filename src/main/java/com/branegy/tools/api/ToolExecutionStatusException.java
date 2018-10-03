package com.branegy.tools.api;

import com.branegy.service.core.exception.ApiException;
import com.branegy.tools.model.ToolHistory.Status;

@SuppressWarnings("serial")
public class ToolExecutionStatusException extends ApiException {
    private final Status toolExecutionStatus;

    public ToolExecutionStatusException(String msg, Status toolExecutionStatus) {
        super(msg);
        this.toolExecutionStatus = toolExecutionStatus;
    }

    public Status getToolExecutionStatus() {
        return toolExecutionStatus;
    }
}
