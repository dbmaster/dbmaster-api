package com.branegy.tools.api;

import java.util.Locale;
import java.util.Map;

import com.branegy.tools.model.ToolConfig;

public interface DataPresenter<T> {

    int generate(ToolConfig config, Map<String,Object> presenterConfig,
            Map<String,Object> paramMap, ExportType type, Locale locale,
            T data, Map<String,Object> headers, ToolOutput out) throws Exception;
}
