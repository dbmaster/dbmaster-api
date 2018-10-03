package com.branegy.tools.api;

import java.io.Flushable;
import java.io.PrintWriter;
import java.util.Locale;

public interface HtmlPrinter extends Flushable{
    //void appendStyle(String css);
    //void appendStyleLink(String css);
    // js / css
    
    // print link!
    
    void print(boolean v);
    void print(char v);
    void print(int v);
    void print(long v);
    void print(float v);
    void print(double v);
    void print(char[] v);
    void print(String v);
    void print(Object v);
    void println();
    void println(boolean v);
    void println(char v);
    void println(int v);
    void println(long v);
    void println(float v);
    void println(double v);
    void println(char[] v);
    void println(String v);
    void println(Object v);
    PrintWriter printf(String t, Object...  param);
    PrintWriter printf(Locale l, String t, Object...param);
    PrintWriter format(String t, Object...p);
    PrintWriter format(Locale l, String t, Object...param);
    
    int pageBreak();
    
    PrintWriter asPrintWriter();
}
