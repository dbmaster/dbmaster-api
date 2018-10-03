package com.branegy.scripting.api;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public interface ScriptConfig {
    ScriptConfig setBindingMap(Map<String,Object> map);
    ScriptConfig setBinding(String key,Object value);
    ScriptConfig setWriter(Writer pw);
    ScriptConfig setErrorWriter(Writer pw);
    ScriptConfig setReader(Reader is);
    
    ScriptResult eval();
}
