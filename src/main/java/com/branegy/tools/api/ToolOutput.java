package com.branegy.tools.api;

import java.io.OutputStream;

public interface ToolOutput {
    OutputStream getOutputStream();
    HtmlPrinter getHtmlPrinter();
}
