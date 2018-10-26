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
@Table(name = "inv_contact")
@CustomFieldDiscriminator("Contact")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_contact where project_id=:projectId")
public class Contact extends BaseCustomEntity {

    public static final String NAME = "ContactName";
    public static final String PHONE = "ContactPhone";
    public static final String EMAIL = "ContactEmail";
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
    
    public String getContactName(){
        return getCustomData(NAME);
    }
    
    public void setContactName(String contactName){
        setCustomData(NAME,contactName);
    }

}