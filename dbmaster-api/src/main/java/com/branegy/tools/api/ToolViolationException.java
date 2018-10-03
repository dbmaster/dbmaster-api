package com.branegy.tools.api;

import java.util.Collections;
import java.util.Map;

import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class ToolViolationException extends ApiException{
    private final Map<String,String> violations;

    public ToolViolationException(Map<String,String> violations) {
        super(violations.toString());
        this.violations = Collections.unmodifiableMap(violations);
    }

    public Map<String, String> getViolations() {
        return violations;
    }
}
