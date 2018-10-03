package com.branegy.scripting.api;

import java.util.Map;

public interface ScriptResult {
    Object getResult();
    <T> T getBinding(String name);
    Map<String,Object> getBindings();
    
    <T> T getInterface(Class<T> clazz);
}
