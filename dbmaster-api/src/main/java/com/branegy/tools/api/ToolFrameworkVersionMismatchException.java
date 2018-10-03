package com.branegy.tools.api;

import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class ToolFrameworkVersionMismatchException extends ApiException {

    public ToolFrameworkVersionMismatchException(String requiredFrameworkVersion,
            String currentFrameworkVersion) {
        super("Can't process "+requiredFrameworkVersion+
                " with framework version "+currentFrameworkVersion);
    }

}
