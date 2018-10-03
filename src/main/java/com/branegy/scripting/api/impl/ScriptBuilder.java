package com.branegy.scripting.api.impl;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

import com.branegy.scripting.api.ScriptApiException;
import com.branegy.scripting.api.ScriptConfig;
import com.branegy.scripting.api.ScriptResult;

class ScriptBuilder implements ScriptConfig, ScriptResult {
    private final ClassLoader contextClassLoader;
    private final ScriptEngine engine;
    private final URL url;
    private final String inline;
    
    private volatile Object result;
    
    public ScriptBuilder(ScriptEngine engine, String inline, ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
        this.engine = engine;
        this.url = null;
        this.inline = inline;
    }

    public ScriptBuilder(ScriptEngine engine, URL url, ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
        this.engine = engine;
        this.url = url;
        this.inline = null;
    }

    @Override
    public ScriptConfig setBindingMap(Map<String, Object> map) {
        ScriptContext ctx = engine.getContext();
        for (Entry<String,Object> e:map.entrySet()){
            ctx.setAttribute(e.getKey(), e.getValue(), ScriptContext.ENGINE_SCOPE);
        }
        return this;
    }

    @Override
    public ScriptConfig setBinding(String key, Object value) {
        engine.getContext().setAttribute(key,value, ScriptContext.ENGINE_SCOPE);
        return this;
    }

    @Override
    public ScriptConfig setWriter(Writer pw) {
        engine.getContext().setWriter(pw);
        return this;
    }

    @Override
    public ScriptConfig setReader(Reader pw) {
        engine.getContext().setReader(pw);
        return this;
    }

    @Override
    public ScriptResult eval(){
        ClassLoader prevContextClassLoader = Thread.currentThread().getContextClassLoader();
        try{
            if (contextClassLoader!=null){
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
            if (url!=null){
                result = evalUrl(engine,url);
            } else {
                result = evalInline(engine,inline);
            }
            return this;
        } catch (ScriptException e){
            // repack InvocationTargetException
            if (e.getCause() instanceof InvocationTargetException) {
                Throwable cause = ((InvocationTargetException)e.getCause()).getTargetException();
                e = new ScriptException(e.getMessage(), e.getFileName(), e.getLineNumber(),
                        e.getColumnNumber());
                e.initCause(cause);
            } else {
                // unpack ScriptException
                while (e.getCause() instanceof ScriptException && e.getCause()!=e) {
                    e = (ScriptException) e.getCause();
                }
            }
            throw new ScriptApiException(e);
        } finally {
            if (contextClassLoader!=null){
                Thread.currentThread().setContextClassLoader(prevContextClassLoader);
            }
        }
    }

    private static Object evalInline(ScriptEngine engine, String inline) throws ScriptException {
        return engine.eval(inline);
    }

    private static Object evalUrl(ScriptEngine engine, URL scriptUrl) throws ScriptException {
        try{
            if (engine instanceof GroovyScriptEngineImpl){
                /*The engine keeps per default hard references to the script functions.
                 * To change this you should set a engine level scoped attribute to the script context of
                 * the name "#jsr223.groovy.engine.keep.globals" with a String being "phantom" to use phantom
                 * references, "weak" to use weak references or "soft" to use soft references - casing is
                 * ignored. Any other string will cause the use of hard references.*/
                // "soft", "weak", "phantom"
                //engine.put("#jsr223.groovy.engine.keep.globals", );
                
                // TODO (Vitali) no cache
                /*Class clazz = (Class)this.classMap.get(script);
                if (clazz != null) {
                  return clazz;
                }*/
                GroovyScriptEngineImpl e = (GroovyScriptEngineImpl) engine;
                GroovyClassLoader groovyClassLoader = e.getClassLoader();
                Class<?> ob = groovyClassLoader.parseClass(new GroovyCodeSource(scriptUrl), false);
                //this.classMap.put(script, clazz);
                
                Method m = GroovyScriptEngineImpl.class
                        .getDeclaredMethod("eval",Class.class, ScriptContext.class);
                m.setAccessible(true);
                try{
                    return m.invoke(e, ob, e.getContext());
                } catch (InvocationTargetException ie){
                    Throwable t = ie.getTargetException();
                    throw (t instanceof Exception?(Exception)t:ie);
                }
            } else {
                return engine.eval(IOUtils.toString(scriptUrl,StandardCharsets.UTF_8));
            }
        } catch (ScriptException e){
            throw e;
        } catch (Exception e){
            throw new ScriptException(e);
        }
    }

    @Override
    public ScriptConfig setErrorWriter(Writer pw) {
        engine.getContext().setErrorWriter(pw);
        return this;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBinding(String name) {
        return (T) engine.get(name);
    }

    @Override
    public Map<String, Object> getBindings() {
        return engine.getBindings(ScriptContext.ENGINE_SCOPE);
    }

    @Override
    public <T> T getInterface(Class<T> clazz) {
        return ((Invocable)engine).getInterface(clazz);
    }

}
