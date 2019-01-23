package com.branegy.inventory.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="inv_application_link")
@Access(AccessType.FIELD)
@CustomFieldDiscriminator("ApplicationLink")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_application_link al " +
        "inner join inv_application a on al.application_id=a.id where a.project_id=:projectId")
public class ApplicationLink extends BaseCustomEntity{
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="job_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Job job;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="security_object_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    SecurityObject securityObject;
    
    // TODO db user

    @ManyToOne(optional=false)
    @JoinColumn(name="application_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Application application;
    
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public final Job getJob() {
        return job;
    }

    public final void setJob(Job job) {
        this.job = job;
    }

    public final SecurityObject getSecurityObject() {
        return securityObject;
    }

    public final void setSecurityObject(SecurityObject securityObject) {
        this.securityObject = securityObject;
    }
}
