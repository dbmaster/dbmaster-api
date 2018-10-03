package com.branegy.tools.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branegy.tools.api.ExportType;

public class OutputEngine {
    protected static final Logger logger = LoggerFactory.getLogger(OutputEngine.class);

    private Set<ExportType> exportType;

    /**
     * Script name that should be executed when user clicks execute button or drill-down is triggered.
     * When export button clicked the same report should be used with additional
     * parameter export with value='Excel'
     */
    private String dataProvider;
    private Map<String,Object> dataProviderConfig;

    private String dataPresenter;
    private Map<String,Object> dataPresenterConfig;
    
    public OutputEngine() {
    }

    public Set<ExportType> getExportType() {
        return exportType;
    }

    public void setExportType(Set<ExportType> type) {
        this.exportType = type;
    }

    public String getDataProvider() {
        return dataProvider;
    }

    public Map<String, Object> getDataProviderConfig() {
        return dataProviderConfig!=null?dataProviderConfig:Collections.<String,Object>emptyMap();
    }

    public String getDataPresenter() {
        return dataPresenter;
    }

    public Map<String, Object> getDataPresenterConfig() {
        return dataPresenterConfig!=null?dataPresenterConfig:Collections.<String,Object>emptyMap();
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void setDataProviderConfig(Map<String, Object> dataProviderConfig) {
        this.dataProviderConfig = dataProviderConfig;
    }

    public void setDataPresenter(String dataPresenter) {
        this.dataPresenter = dataPresenter;
    }

    public void setDataPresenterConfig(Map<String, Object> dataPresenterConfig) {
        this.dataPresenterConfig = dataPresenterConfig;
    }
}
