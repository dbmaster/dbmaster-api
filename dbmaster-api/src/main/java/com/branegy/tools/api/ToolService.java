package com.branegy.tools.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;
import com.branegy.tools.model.AdhocReportConfig;
import com.branegy.tools.model.ToolConfig;
import com.branegy.tools.model.ToolHistory;

public interface ToolService {
    
    ToolConfig findToolConfig(String toolId);
    List<ToolConfig> getToolList();

    AdhocReportConfig findQuickLink(String toolId, String shortcut);
    AdhocReportConfig saveQuickLink(AdhocReportConfig report);
    void deleteQuickLink(AdhocReportConfig model);
    Slice<AdhocReportConfig> getQuickLinkSlice(String toolId, QueryRequest request);
    
    Slice<ToolHistory> getToolHistorySlice(QueryRequest request);
    ToolHistory findToolHistoryById(long executionId, String fetchPath);

    interface HtmlToolExecutor{
        long getExecutionId();
        /**
         * @return
         * @throws ExecutionException
         * @throws CancellationException
         * @throws InterruptedException
         */
        String execute() throws ExecutionException, InterruptedException;
    }
    HtmlToolExecutor toolExecutor(String toolId, Map<String,Object> parameters);
    
    interface DataToolExecutor<T>{
        long getExecutionId();
        ResultWithHeader<T> execute() throws ExecutionException, InterruptedException;
    }
    <T> DataToolExecutor<T> toolExecutor(String toolId, Map<String,Object> parameters, ExportType exportType);
    
    boolean stopExecution(long executionId);
    boolean stopExecutionAndWait(long executionId, long timeout);
    ToolLogReader getToolLog(long executionId);
    
    Map<String,Object> validateToolParameters(ToolConfig tool, Map<String, Object> parameters);
}