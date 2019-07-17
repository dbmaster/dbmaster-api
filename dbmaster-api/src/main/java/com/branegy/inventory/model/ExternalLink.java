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
@Table(name="inv_external_link")
@Access(AccessType.FIELD)
@CustomFieldDiscriminator(ExternalLink.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_external_link el " +
        "where ((el.application is not null) and (el.application.project_id=:projectId))")
public class ExternalLink extends BaseCustomEntity{
    static final String CUSTOM_FIELD_DISCRIMINATOR = "ExternalLink";
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
