package com.branegy.service.base.api;

import java.util.List;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;

public interface ProjectService {
    
    Project getCurrentProject();

    Project findProjectById(long id);

    Project findProjectByName(String name);

    // TODO rename to saveProject
    Project mergeProject(Project project);

    void deleteProject(long id);

    // TODO rename to createProject
    Project persistProject(String name, String description, String type);

    List<Project> getProjectList();
    List<Project> getProjectList(String type);

    List<Project> getProjectList(User user);
    List<Project> getProjectList(User user, String type);

}