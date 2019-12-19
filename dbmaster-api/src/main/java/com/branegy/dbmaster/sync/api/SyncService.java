package com.branegy.dbmaster.sync.api;

import java.util.List;

import com.branegy.service.core.QueryRequest;

public interface SyncService {
    
    /**
     * @param session - session to save
     * @param sessionType - identifier to distinguish different session types
     * @return sessionId
     */
    long saveSession(SyncSession session, String sessionType);
    
    List<SyncSession> getSessions(QueryRequest request, boolean includePairs);
    
    SyncSession findSessionById(long sessionId, boolean includePairs);

    String generateSyncSessionPreviewHtml(SyncSession syncSession, boolean showChangesOnly);
    String generateSyncSessionPreviewHtml(List<SyncSession> syncSessionList, boolean showChangesOnly);
    
    String generateSyncSessionPreviewHtml(String templateName, SyncSession syncSession, boolean showChangesOnly);

    List<SyncPair> getSyncPairsByName(String... typeName);
   
    SummaryGenerator getSummaryGenerator(String templateName);
}