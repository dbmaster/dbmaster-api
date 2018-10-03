package com.branegy.tools.api;

import java.io.Closeable;
import java.util.Enumeration;

import com.branegy.tools.api.ToolLogEntry;

//don't thread safe
public interface ToolLogReader extends Enumeration<ToolLogEntry>,Closeable{

    boolean hasMoreElements();
    ToolLogEntry nextElement();

    void close();
}