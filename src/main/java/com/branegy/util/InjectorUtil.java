package com.branegy.util;

import com.google.inject.Inject;
import com.google.inject.Injector;

public abstract class InjectorUtil {
    @Inject
    private static volatile Injector injector;

    private InjectorUtil() {
    }

    public static <T> T getInstance(Class<T> clazz){
        return injector.getInstance(clazz);
    }

    public static Injector getInjector(){
        return injector;
    }

}
