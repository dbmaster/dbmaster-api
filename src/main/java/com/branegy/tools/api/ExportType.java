package com.branegy.tools.api;

/**
 *  constant order must be the same as in com.branegy.gwt.report.client.model.UIReportConfig.ExportType
 */
public enum ExportType {
     XSLX, PDF, HTML;
     
     public String getExtension(){
         return name().toLowerCase();
     }
}