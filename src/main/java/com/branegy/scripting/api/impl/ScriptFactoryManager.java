package com.branegy.scripting.api.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.branegy.scripting.api.ScriptApiException;
import com.branegy.scripting.api.ScriptConfig;
import com.branegy.util.DataDirHelper;

public class ScriptFactoryManager implements com.branegy.scripting.api.ScriptFactory {

    @Override
    public ScriptConfig scriptFromFile(String scriptFileName) {
        ScriptEngine engine = getEngine(FilenameUtils.getExtension(scriptFileName), null);
        try {
            return new ScriptBuilder(engine,
                    new File(DataDirHelper.getDataDir()+"scripts/"+scriptFileName).toURI().toURL(), null);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private ScriptEngine getEngine(String engineExtension, ClassLoader contextClassLoader) {
        ClassLoader prevContextClassLoader = Thread.currentThread().getContextClassLoader();
        try{
            if (contextClassLoader!=null){
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
            ScriptEngine engine = new ScriptEngineManager(ScriptFactoryManager.class.getClassLoader())
                .getEngineByExtension(engineExtension);
            if (engine == null){
                throw new ScriptApiException(new ScriptException("Engine is not found for "+engineExtension));
            }
            return engine;
        } finally{
            if (contextClassLoader!=null){
                Thread.currentThread().setContextClassLoader(prevContextClassLoader);
            }
        }
    }

    @Override
    public ScriptConfig scriptFromFile(File file) {
        ScriptEngine engine = getEngine(FilenameUtils.getExtension(file.getName()), null);
        try {
            return new ScriptBuilder(engine, file.toURI().toURL(), null);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public ScriptConfig inlineScript(String engineName, String script) {
        ScriptEngine engine = getEngine(engineName, null);
        return new ScriptBuilder(engine, script, null);
    }

    @Override
    public ScriptConfig scriptFromUrl(URL url) {
        return scriptFromUrl((ClassLoader)null, url);
    }

    @Override
    public ScriptConfig scriptFromUrl(ClassLoader contextCl, URL url) {
        return scriptFromUrl(FilenameUtils.getExtension(url.getPath()), contextCl, url);
    }

    @Override
    public ScriptConfig scriptFromUrl(String engineName, ClassLoader contextCl, URL url) {
        ScriptEngine engine = getEngine(engineName, contextCl);
        return new ScriptBuilder(engine, url, contextCl);
    }

    @Override
    public ScriptConfig scriptFromUrl(String engineName, URL url) {
        return scriptFromUrl(engineName, null, url);
    }
    
    public void exportExcel(String templateName, String scriptName, OutputStream outStream,Locale locale,
            Map<String,Object> bindings)
            throws IOException {
        FileInputStream fis = null;
        try{
            String name =  FilenameUtils.removeExtension(templateName);
            String ext = FilenameUtils.getExtension(templateName);
            String dir = DataDirHelper.getDataDir()+"templates/";
            String templatePath;

            if (new File(dir+name+"_"+locale.getLanguage()+"_"+locale.getCountry()+"."+ext).isFile()){
                templatePath = dir+name+"_"+locale.getLanguage()+"_"+locale.getCountry()+"."+ext;
            } else if (new File(dir+name+"_"+locale.getLanguage()+"."+ext).isFile()){
                templatePath = dir+name+"_"+locale.getLanguage()+"."+ext;
            } else {
                templatePath = dir+templateName;
            }

            fis = new FileInputStream(templatePath);
            scriptFromFile(scriptName)
                .setBinding("templateInputStream", new BufferedInputStream(fis))
                .setBinding("outputStream", outStream)
                .setBindingMap(bindings)
                .eval();
        } finally{
            IOUtils.closeQuietly(fis);
        }
    }

}
