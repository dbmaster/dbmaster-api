package com.branegy.inventory.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "inv_job")
@CustomFieldDiscriminator("Job")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_job where project_id=:projectId")
public class Job extends BaseCustomEntity {
    
    public static final String JOB_NAME = "JobName";
    public static final String SERVER_NAME = "ServerName";
    public static final String JOB_TYPE = "JobType";
    public static final String DELETED = "Deleted";
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;

    public final Project getProject() {
        return project;
    }
    public final void setProject(Project project) {
        this.project = project;
    }

    public String getServerName() {
        return getCustomData(SERVER_NAME);
    }
  
    public void setServerName(String serverName) {
        setCustomData(SERVER_NAME, serverName);
    }
    
    public String getJobName() {
        return getCustomData(JOB_NAME);
    }
    
    public void setJobName(String jobName) {
        setCustomData(JOB_NAME, jobName);
    }
    
    public String getJobType() {
        return getCustomData(JOB_TYPE);
    }
    
    public void setJobType(String type) {
        setCustomData(JOB_TYPE, type);
    }
    
    public boolean isDeleted() {
        Boolean deleted = getCustomData(DELETED);
        return deleted!=null && deleted;
    }
    
}
