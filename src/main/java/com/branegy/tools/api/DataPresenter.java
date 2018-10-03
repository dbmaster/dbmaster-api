package com.branegy.tools.api;

import java.util.Locale;
import java.util.Map;

import com.branegy.tools.model.ToolConfig;

public interface DataPresenter<T> {
    /**
     * @param config
     * @param presenterConfig
     * @param paramMap
     * @param type
     * @param out
     * @return -1
     */
    int generate(ToolConfig config, Map<String,Object> presenterConfig,
            Map<String,Object> paramMap, ExportType type, Locale locale,
            T data, Map<String,Object> headers, ToolOutput out) throws Exception;
}
