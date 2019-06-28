package com.branegy.inventory.model;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.SortedMap;

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

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name = "inv_job")
@CustomFieldDiscriminator(Job.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_job where project_id=:projectId")
public class Job extends BaseCustomEntity {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Job";
    
    public static final String JOB_NAME = "JobName";
    public static final String SERVER_NAME = "ServerName";
    public static final String JOB_TYPE = "JobType";
    public static final String DELETED = "Deleted";
    public static final String LAST_SYNC_DATE = "Last Sync Date";
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
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
    
    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=BaseCustomEntity.CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }
    
}
