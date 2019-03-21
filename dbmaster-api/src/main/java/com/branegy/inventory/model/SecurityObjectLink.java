package com.branegy.inventory.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name = "inv_security_object_link")
@CustomFieldDiscriminator("SecurityObjectLink")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_security_object_link where project_id=:projectId")
@NamedQueries({
    @NamedQuery(name=SecurityObjectLink.QUERY_FIND_ALL_SECURITY_OBJECT_LINK_BY_PROJECT, 
        query="from SecurityObjectLink where project.id=:projectId"),
    @NamedQuery(name=SecurityObjectLink.QUERY_FIND_ALL_SECURITY_OBJECT_LINK_BY_PROJECT_ID, 
        query="from SecurityObjectLink where " + 
            "project.id=:projectId and "
                + "(sourceObject.id=:id or targetObject.id=:id)"),
    @NamedQuery(name=SecurityObjectLink.QUERY_FIND_ACTIVE_SECURITY_OBJECT_LINK_BY_PROJECT_ID, 
        query="from SecurityObjectLink where " + 
            "project.id=:projectId and "
                + "(sourceObject.id=:id or targetObject.id=:id) and "
                + "deleted = false and "
                + "sourceObject.deleted=false and "
                + "targetObject.deleted=false"),
})
public class SecurityObjectLink extends BaseCustomEntity {
    public static final String QUERY_FIND_ALL_SECURITY_OBJECT_LINK_BY_PROJECT = "SecurityObjectLink.findAllByProject";
    public static final String QUERY_FIND_ALL_SECURITY_OBJECT_LINK_BY_PROJECT_ID = "SecurityObjectLink.findAllByProjectId";
    public static final String QUERY_FIND_ACTIVE_SECURITY_OBJECT_LINK_BY_PROJECT_ID = "SecurityObjectLink.findActiveAllByProjectId";
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id",nullable=false)
    Project project;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="source_object_id",nullable=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    SecurityObject sourceObject;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="target_object_id",nullable=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    SecurityObject targetObject;
    
    @Column(name="IS_AUTO_DETECTED")
    boolean autodetected;
    
    @Column(name="deleted")
    boolean deleted;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public SecurityObject getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(SecurityObject sourceObject) {
        this.sourceObject = sourceObject;
    }

    public SecurityObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(SecurityObject targetObject) {
        this.targetObject = targetObject;
    }

    public boolean isAutodetected() {
        return autodetected;
    }

    public void setAutodetected(boolean autodetected) {
        this.autodetected = autodetected;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}