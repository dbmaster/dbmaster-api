package com.branegy.inventory.model;

import static com.branegy.persistence.custom.EmbeddableObject.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableObject.ENTITY_ID_COLUMN;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableObject;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="inv_application_link")
@Access(AccessType.FIELD)
@CustomFieldDiscriminator(ApplicationLink.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_application_link al " +
        "inner join inv_application a on al.application_id=a.id where a.project_id=:projectId")
public class ApplicationLink extends BaseCustomEntity{
    static final String CUSTOM_FIELD_DISCRIMINATOR = "ApplicationLink";

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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public SecurityObject getSecurityObject() {
        return securityObject;
    }

    public void setSecurityObject(SecurityObject securityObject) {
        this.securityObject = securityObject;
    }
    
    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected List<EmbeddableObject> getCustom() {
        return getInnerCustomList();
    }
}
