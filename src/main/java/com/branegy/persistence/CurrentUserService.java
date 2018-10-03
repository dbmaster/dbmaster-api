package com.branegy.persistence;

// TODO Review Name
public abstract class CurrentUserService {
    private static volatile ICurrentUserService impl;

    private CurrentUserService() {
    }

    public interface ICurrentUserService {
        String getCurrentUser();
    }

    public static String getCurrentUser() {
        return impl.getCurrentUser();
    }
    
    public static String getCurrentUser(int maxLength){
        String currentUser = getCurrentUser();
        return (currentUser==null)?null:currentUser.substring(0,Math.min(currentUser.length(),maxLength));
    }

    public static void setImpl(ICurrentUserService impl) {
        CurrentUserService.impl = impl;
    }

}
