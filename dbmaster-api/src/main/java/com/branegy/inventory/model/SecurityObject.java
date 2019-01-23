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
@Table(name = "inv_security_object")
@CustomFieldDiscriminator("SecurityObject")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_security_object where project_id=:projectId")
public class SecurityObject extends BaseCustomEntity {
    
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
    
    public String getSourceName() {
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
        Boolean deleted = getCustomData(DELETED);
        return deleted!=null && deleted;
    }
    
}
