package com.branegy.tools.api;

import java.util.Set;

import com.branegy.dbmaster.core.User;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;
import com.branegy.tools.model.ToolPermission;
import com.branegy.tools.model.ToolPermission.ToolAccess;

public interface ToolPermissionService {
    ToolPermission findToolPermissionById(long id);
    ToolPermission createToolPermission(User user, Set<ToolAccess> role, String toolId);
    ToolPermission mergeToolPermission(ToolPermission permission);
    void deleteToolPermission(ToolPermission permission);
    
    Slice<ToolPermission> getToolPermissionSlice(String toolId, QueryRequest request);
}
