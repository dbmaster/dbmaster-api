package com.branegy.dbmaster.database.api;

import com.branegy.dbmaster.model.SyncSessionDataSource;
import com.branegy.dbmaster.sync.api.SyncService;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface ModelSyncSessionService extends SyncService {
    Slice<SyncSessionDataSource> getSessionsSlice(QueryRequest request, boolean includePairs);
}
