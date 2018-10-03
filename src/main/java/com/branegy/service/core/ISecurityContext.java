package com.branegy.service.core;

import com.branegy.dbmaster.core.Permission.Role;
import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;

public interface ISecurityContext {
    Long getCurrentUserId();
    Long getCurrentProjectId();

    Project getCurrentProject();
    User getCurrentUser();

    boolean isUserAdmin();
    boolean isUserCreateProject();
    boolean isUserInRole(Role... roles);
    
    // don't use it
    boolean isUserInRole(Project project, Role... roles);
}
