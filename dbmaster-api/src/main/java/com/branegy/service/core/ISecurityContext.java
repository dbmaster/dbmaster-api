package com.branegy.service.core;

import com.branegy.dbmaster.core.Permission.Role;
import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;

public interface ISecurityContext {
    ISecurityContext DEFAULT = new ISecurityContext() {
        @Override
        public boolean isUserInRole(Project project, Role... roles) {
            return false;
        }
        @Override
        public boolean isUserInRole(Role... roles) {
            return false;
        }
        @Override
        public boolean isUserCreateProject() {
            return false;
        }
        @Override
        public boolean isUserAdmin() {
            return false;
        }
        @Override
        public Long getCurrentUserId() {
            return null;
        }
        @Override
        public User getCurrentUser() {
            return null;
        }
        @Override
        public Long getCurrentProjectId() {
            return null;
        }
        @Override
        public Project getCurrentProject() {
            return null;
        }
    }; 
    
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
