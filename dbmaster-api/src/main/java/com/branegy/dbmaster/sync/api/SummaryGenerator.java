package com.branegy.dbmaster.sync.api;

import java.util.List;
import com.branegy.service.core.QueryRequest;

public interface SummaryGenerator {
     
    String generateSummary(SyncSession session);

    String generateSummary(SyncPair pair);

    void setShowChangesOnly(boolean showChangesOnly);  
}