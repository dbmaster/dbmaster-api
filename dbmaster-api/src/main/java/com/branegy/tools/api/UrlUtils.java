package com.branegy.tools.api;

public abstract class UrlUtils {

    private UrlUtils() {
    }
    
    public static String encodePath(String token) {
        if (token.indexOf('/') == -1) {
            return token;
        } else {
            return '\''+token.replace("'", "''")+'\'';
        }
    }

}
