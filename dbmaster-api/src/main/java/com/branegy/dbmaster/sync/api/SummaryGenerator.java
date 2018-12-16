package com.branegy.dbmaster.sync.api;

import java.util.List;
import com.branegy.service.core.QueryRequest;

public interface SummaryGenerator {
    public static final String SHOW_CHANGES_ONLY = "showChangesOnly";
     
    String generateSummary(SyncSession session);

    String generateSummary(SyncPair pair);

    void setParameter(String parameterName, Object value);  
}   