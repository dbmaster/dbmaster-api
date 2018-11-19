package com.branegy.scripting.api;

import java.io.File;
import java.net.URL;

public interface ScriptFactory {
    
    ScriptConfig scriptFromFile(String file);
    ScriptConfig scriptFromFile(File file);
    ScriptConfig scriptFromFile(String... files);
    ScriptConfig scriptFromFile(File... files);

    ScriptConfig scriptFromUrl(URL url);
    ScriptConfig scriptFromUrl(ClassLoader cl, URL url);
    ScriptConfig scriptFromUrl(String engineName, URL url);
    ScriptConfig scriptFromUrl(String engineName, ClassLoader cl, URL url);
    
    
    ScriptConfig inlineScript(String engineName, String script);
}
