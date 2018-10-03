package com.branegy.scripting;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

import com.branegy.service.core.exception.ApiException;
import com.branegy.service.core.exception.IllegalStateApiException;
import com.branegy.util.DataDirHelper;

public class ScriptExecutor {
    protected final Map<String, Object> variables = new HashMap<String,Object>();

    private volatile String progressMsg = null;
    private volatile float done = 0;
    private volatile boolean canceled = false;
    private volatile ScriptEngine engine = null;
    
    public void setVariable(String variableName,Object value) {
        variables.put(variableName,value);
    }

    public void setProgress(float done, String progressMsg){
        this.done = done;
        this.progressMsg = progressMsg;
    }

    public void setProgressDone(float done){
        this.done = done;
    }

    public void setProgressMsg(String progressMsg){
        this.progressMsg = progressMsg;
    }

    public void cancel(){
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public float getDone() {
        return done;
    }

    public String getMessage() {
        return progressMsg;
    }

    public String runScriptInFile(String scriptFileName) {
        engine = getEngine();
        InputStream is = null;
        try {
            URL scriptUrl = new File(DataDirHelper.getDataDir()+"scripts/"+scriptFileName).toURI().toURL();
            eval(engine,scriptUrl);
            return (String)engine.get("log");
        } catch (Exception e) {
            throw new ApiException(e);
        } finally {
            try {
                if (is!=null) is.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String runScriptInline(String script) throws Exception {
        ScriptEngine engine = getEngine();
        engine.eval(script,engine.getBindings(ScriptContext.ENGINE_SCOPE));
        return (String)engine.get("log");
    }
    
    protected synchronized ScriptEngine getEngine() {
        if (engine==null) {
            /**
             * Workaround for Groovy + OSGi and ScriptEngine class load discovery.
             * This code will be refactored while DbMaser is fully OSGi for all modules included Groovy as
             * bundle
             */
            engine = new ScriptEngineManager(ScriptExecutor.class.getClassLoader())
                .getEngineByExtension("groovy");
            
            if (engine==null) {
                throw new ApiException("Cannot initialize groovy engine");
            }
            engine.put("helper", this);
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                engine.put(entry.getKey(), entry.getValue());
            }
        }
        return engine;
    }
    
    public Object getBinding(String name){
        return engine.get(name);
    }

    public static Class<?> parseClass(ClassLoader parentCL, String classpath, File file) throws Exception {
        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setClasspath(classpath);
        @SuppressWarnings("resource")
        GroovyClassLoader loader = new GroovyClassLoader(parentCL, compilerConfig);
        Class<?> groovyClass = loader.parseClass(file);
        return groovyClass;

        // let's call some method on an instance
//        GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
//        Object[] args = {};
//        groovyObject.invokeMethod("run", args);

    }
    
    @Deprecated
    // Temporary helper method, refactoring required
    public static void eval(ScriptEngine engine, URL scriptUrl){
        try{
            if (engine instanceof GroovyScriptEngineImpl){
                // TODO (Vitali) no cache
                GroovyScriptEngineImpl e = (GroovyScriptEngineImpl) engine;
                Class<?> ob = e.getClassLoader().parseClass(new GroovyCodeSource(scriptUrl));
                Method m = GroovyScriptEngineImpl.class.getDeclaredMethod("eval",
                        Class.class, ScriptContext.class);
                m.setAccessible(true);
                m.invoke(e, ob, e.getContext());
            } else {
                engine.eval(IOUtils.toString(scriptUrl,"UTF-8"));
            }
        } catch (Exception e){
            throw new IllegalStateApiException(e);
        }
    }

}
