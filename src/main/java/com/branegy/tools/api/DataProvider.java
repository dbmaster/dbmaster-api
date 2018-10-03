package com.branegy.tools.api;

import java.util.Map;

import org.slf4j.Logger;

import com.branegy.scripting.DbMaster;
import com.branegy.tools.model.OutputEngine;
import com.branegy.tools.model.ToolConfig;

//CHECKSTYLE:OFF
public interface DataProvider<T> {
    T getData(ToolConfig config, OutputEngine engine, Logger logger,
            DbMaster dbm, Map<String,Object> params) throws Throwable;
}
