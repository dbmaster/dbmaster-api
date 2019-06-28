package com.branegy.inventory.model;

import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_BY_APPLICATION;
import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_BY_DATABASE;
import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_BY_INSTALLATION;
import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_BY_PROJECT;
import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_COUNT_BY_APPLICATION;
import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_COUNT_BY_DATABASE;
import static com.branegy.inventory.model.DatabaseUsage.QUERY_DB_USAGE_COUNT_BY_INSTALLATION;
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

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="inv_database_usage")
@CustomFieldDiscriminator(DatabaseUsage.CUSTOM_FIELD_DISCRIMINATOR)
@NamedQueries( {
    @NamedQuery(name = QUERY_DB_USAGE_BY_INSTALLATION, query = "from DatabaseUsage du "
            + "where du.installation.id=:installation_id"),
    @NamedQuery(name = QUERY_DB_USAGE_COUNT_BY_INSTALLATION,
        query = "select count(du) from DatabaseUsage du "
            + "where du.installation.id=:installation_id"),
    @NamedQuery(name = QUERY_DB_USAGE_BY_DATABASE, query = "from DatabaseUsage du "
            + "where du.database.id=:databaseId"),
    @NamedQuery(name = QUERY_DB_USAGE_COUNT_BY_DATABASE,
        query = "select count(du) from DatabaseUsage du "
            + "where du.database.id=:databaseId"),
    @NamedQuery(name = QUERY_DB_USAGE_BY_APPLICATION, query = "from DatabaseUsage du "
            + "where du.application.id=:applicationId"),
    @NamedQuery(name = QUERY_DB_USAGE_COUNT_BY_APPLICATION,
        query = "select count(du) from DatabaseUsage du "
            + "where du.application.id=:applicationId"),
    @NamedQuery(name = QUERY_DB_USAGE_BY_PROJECT,
        query = "from DatabaseUsage du where du.database.project.id=:projectId")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_database_usage du " +
        "inner join inv_database d on du.database_id=d.id where d.project_id=:projectId")
public class DatabaseUsage extends BaseCustomEntity{
    static final String CUSTOM_FIELD_DISCRIMINATOR = "DatabaseUsage";
    public static final String QUERY_DB_USAGE_BY_INSTALLATION = "DBUsage.findByInstallation";
    public static final String QUERY_DB_USAGE_COUNT_BY_INSTALLATION = "DBUsage.findCountByInstallation";
    public static final String QUERY_DB_USAGE_BY_DATABASE = "DBUsage.findByDatabase";
    public static final String QUERY_DB_USAGE_COUNT_BY_DATABASE = "DBUsage.findCountByDatabase";
    public static final String QUERY_DB_USAGE_BY_APPLICATION = "DBUsage.findByApplication";
    public static final String QUERY_DB_USAGE_COUNT_BY_APPLICATION = "DBUsage.findCountByApplication";
    public static final String QUERY_DB_USAGE_BY_PROJECT = "DBUsage.findByProject";

    public static final String USAGE_NOTES = "UsageNotes";
    public static final String USAGE_ROLES = "UsageRoles";
    public static final String LAST_SYNC_DATE = "Last Sync Date";

    @ManyToOne(optional=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @BatchSize(size=50)
    @JoinColumn(name="database_id")
    Database database;

    @ManyToOne(optional=true)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @BatchSize(size=50)
    @JoinColumn(name="installation_id")
    Installation installation;

    @ManyToOne(optional=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @BatchSize(size=50)
    @JoinColumn(name="application_id")
    Application application;

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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
