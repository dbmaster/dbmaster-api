package com.branegy.dbmaster.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import com.google.common.base.Joiner;

@SuppressWarnings("serial")
// TODO (Vitali) implement full logger
public class StringLogger extends MarkerIgnoringBase {
    private final List<String> builder = Collections.synchronizedList(new ArrayList<String>());

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String paramString) {
    }

    @Override
    public void trace(String paramString, Object paramObject) {
    }

    @Override
    public void trace(String paramString, Object paramObject1, Object paramObject2) {
    }

    @Override
    public void trace(String paramString, Object... paramArrayOfObject) {
    }

    @Override
    public void trace(String paramString, Throwable paramThrowable) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String paramString) {
    }

    @Override
    public void debug(String paramString, Object paramObject) {
    }

    @Override
    public void debug(String paramString, Object paramObject1, Object paramObject2) {
    }

    @Override
    public void debug(String paramString, Object... paramArrayOfObject) {
    }

    @Override
    public void debug(String paramString, Throwable paramThrowable) {
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String paramString) {
    }

    @Override
    public void info(String paramString, Object paramObject) {
    }

    @Override
    public void info(String paramString, Object paramObject1, Object paramObject2) {
    }

    @Override
    public void info(String paramString, Object... paramArrayOfObject) {
    }

    @Override
    public void info(String paramString, Throwable paramThrowable) {
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String paramString) {
    }

    @Override
    public void warn(String paramString, Object paramObject) {
    }

    @Override
    public void warn(String paramString, Object... paramArrayOfObject) {
    }

    @Override
    public void warn(String paramString, Object paramObject1, Object paramObject2) {
    }

    @Override
    public void warn(String paramString, Throwable paramThrowable) {
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String paramString) {
        builder.add("ERROR: "+paramString);
    }

    @Override
    public void error(String paramString, Object paramObject) {
        builder.add("ERROR: "+MessageFormatter.format(paramString, paramObject));
    }

    @Override
    public void error(String paramString, Object paramObject1, Object paramObject2) {
        builder.add("ERROR: "+MessageFormatter.format(paramString, paramObject1, paramObject2));
    }

    @Override
    public void error(String paramString, Object... paramArrayOfObject) {
        builder.add("ERROR: "+MessageFormatter.arrayFormat(paramString, paramArrayOfObject));
    }

    @Override
    public void error(String paramString, Throwable paramThrowable) {
        builder.add("ERROR: "+MessageFormatter.format(paramString, paramThrowable));
    }

    @Override
    public String toString(){
        return Joiner.on("\r\n").join(builder);
    }
}
