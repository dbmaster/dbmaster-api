package com.branegy.service.core.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

public class ValidationApiException extends ApiException {
    private static final long serialVersionUID = -8091252903063447920L;

    public ValidationApiException(ConstraintViolationException validationException) {
        super(validationException);
    }

    @Override
    public int getErrorCode() {
        return 511;
    }

    @Override
    public ConstraintViolationException getCause() {
        return (ConstraintViolationException) super.getCause();
    }

    @Override
    public String getMessage() {
        return ""+getCause().getConstraintViolations();
    }
    
    public String getInterpolatedMessage(){
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> e:getCause().getConstraintViolations() ){
            sb.append("\n");
            sb.append(e.getMessage());
        }
        return sb.substring(1);
    }

}
