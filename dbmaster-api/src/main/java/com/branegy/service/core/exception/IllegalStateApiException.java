package com.branegy.service.core.exception;

@SuppressWarnings("serial")
public class IllegalStateApiException extends ApiException {


    public IllegalStateApiException() {
    }

    public IllegalStateApiException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }

    public IllegalStateApiException(String paramString) {
        super(paramString);
    }

    public IllegalStateApiException(Throwable paramThrowable) {
        super(paramThrowable);
    }

    @Override
    public int getErrorCode() {
        return 501;
    }
    
    public static void rethrow(Throwable t){
        if (t instanceof ApiException){
            throw (ApiException)t;
        }
        throw new IllegalStateApiException(t);
    }
    
    public static void rethrow(String msg, Throwable t){
        if (t instanceof ApiException){
            throw (ApiException)t;
        }
        throw new IllegalStateApiException(msg, t);
    }
    
    public static ApiException wrap(String msg, Throwable t){
        if (t instanceof ApiException){
            return (ApiException)t;
        }
        return new IllegalStateApiException(msg, t);
    }
    
    public static ApiException wrap(Throwable t){
        if (t instanceof ApiException){
            return (ApiException)t;
        }
        return new IllegalStateApiException(t);
    }

}
