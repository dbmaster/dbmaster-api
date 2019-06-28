package com.branegy.inventory.model;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
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
@Table(name = "inv_security_object")
@CustomFieldDiscriminator(SecurityObject.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_security_object where project_id=:projectId")
public class SecurityObject extends BaseCustomEntity {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "SecurityObject";
    
    public static final String SOURCE = "Source";
    public static final String SERVER_NAME = "ServerName";
    public static final String ID = "Id";
    public static final String NAME = "Name";
    public static final String TYPE = "Type";
    public static final String ENABLED = "Enabled";
    public static final String DESCRIPTION = "Description";
    public static final String NOTES = "Notes";
    public static final String LAST_SYNC_DATE = "Last Sync Date";
    public static final String LAST_UPDATE_DATE = "Last Update Date";
    public static final String MEMBERS = "Members";
    
    public static final String DELETED = "Deleted";
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;
    
    @Column(name="deleted")
    boolean deleted;

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
    
    public String getSource() {
        return getCustomData(SOURCE);
    }
    
    public void setSource(String source) {
        setCustomData(SOURCE, source);
    }
    
    public String getSecurityObjectId() {
        return getCustomData(ID);
    }
    
    public void setSecurityObjectId(String id) {
        setCustomData(ID, id);
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
