package com.branegy.tools.api;

import java.util.Locale;
import java.util.Map;

import com.branegy.tools.model.ToolConfig;

// sub report helper
// page break!
// event for pages
// print writer
public interface HtmlDataPresenter<T> extends DataPresenter<T> {
    /*
     * @param pageIndex - page started from 0, if <0 - all page required
     * @param returnPageCount if true -
     */
    void configurePresenter(int pageIndex, boolean returnPageCount);

    /*
     * @return page count if returnPageCount set to true, of -1
     */
    int generate(ToolConfig config, Map<String,Object> presenterConfig, Map<String,Object> paramMap,
            ExportType type, Locale locale, T data, Map<String,Object> headers, ToolOutput out);
}
