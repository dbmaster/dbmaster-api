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
@Table(name="inv_external_link")
@Access(AccessType.FIELD)
@CustomFieldDiscriminator("ExternalLink")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_external_link el " +
        "where ((el.application is not null) and (el.application.project_id=:projectId))")
public class ExternalLink extends BaseCustomEntity{
    public static final String URL = "URL";
    public static final String LINK_TEXT = "LinkText";
    public static final String LINK_TYPE = "LinkType";
    public static final String COMMENTS = "Comments";

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="application_id",updatable=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
