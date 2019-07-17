package com.branegy.inventory.model;

import static com.branegy.inventory.model.Application.QUERY_APPLICATION_ALL;
import static com.branegy.inventory.model.Application.QUERY_APPLICATION_BY_DATABASE;
import static com.branegy.inventory.model.Application.QUERY_COUNT_APPLICATION_BY_DATABASE;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
import com.branegy.persistence.custom.EmbeddableObject;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@NamedQueries({
    @NamedQuery(name = QUERY_APPLICATION_ALL,
        query = "select a from Application a where a.project.id=:project_id"),
    @NamedQuery(name = QUERY_APPLICATION_BY_DATABASE,
        query = "select du.application from DatabaseUsage du " +
        "where du.database.id=:databaseId and du.database.project.id=:project_id"),
    @NamedQuery(name = QUERY_COUNT_APPLICATION_BY_DATABASE,
        query = "select count(du.application.id) from DatabaseUsage du " +
    "where du.database.id=:databaseId and du.database.project.id=:project_id")
})
@CustomFieldDiscriminator(Application.CUSTOM_FIELD_DISCRIMINATOR)
@Table(name="inv_application")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_application where project_id=:projectId")
/**
 * Model schema:
 * 
 * Application -> DatabaseUsage <- Database (Installation)
 * Application -> ContactLink <- Contact
 * Application -> Installation <- Server
 * 
 * Server -> ContactLink <- Contact
 * Server -> Installation
 * 
 * Database -> DatabaseUsage <- Application (Installation)
 * 
 * Contact -> ContactLink
 * 
 * @author keygen
 *
 */
public class Application extends BaseCustomEntity {
    public static final String CUSTOM_FIELD_DISCRIMINATOR = "Application";
    public static final String QUERY_APPLICATION_ALL = "Application.findAll";
    public static final String QUERY_APPLICATION_BY_DATABASE = "Application.findByDatabase";
    public static final String QUERY_COUNT_APPLICATION_BY_DATABASE = "Application.findCountByDatabase";

    public static final String APPLICATION_NAME = "ApplicationName";
    public static final String LAST_SYNC_DATE = "Last Sync Date";


    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getApplicationName(){
        return getCustomData(APPLICATION_NAME);
    }
    
    public void setApplicationName(String applicationName){
        setCustomData(APPLICATION_NAME,applicationName);
    }

    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    /*@OrderBy(com.branegy.persistence.custom.EmbeddableObject.ENTITY_ID_COLUMN+", "+
             //com.branegy.persistence.custom.EmbeddableObject.CLAZZ_COLUMN+", "+
             com.branegy.persistence.custom.EmbeddableObject.KEY_COLUMN+", "+
             com.branegy.persistence.custom.EmbeddableObject.ORDER_COLUMN
            )
    @CollectionId(columns = {
            @Column(name = "id")
            
            },
            type = @Type(type = "string"), generator = "indentity")*/
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected List<EmbeddableObject> getCustom() {
        return getInnerCustomList();
    }
}
