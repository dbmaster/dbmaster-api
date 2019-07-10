package com.branegy.inventory.model;

import static com.branegy.inventory.model.Database.QUERY_DATABASE_BY_SERVER_PROJECT;
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
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name = "inv_database")
@CustomFieldDiscriminator(Database.CUSTOM_FIELD_DISCRIMINATOR)
@NamedQueries({
    @NamedQuery(name = QUERY_DATABASE_BY_SERVER_PROJECT,
        query = "from Database d where " +
                "d.project.id=:projectId and UPPER(d.databaseServer)=UPPER(:connectionName)")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_database where project_id=:projectId")
public class Database extends BaseCustomEntity implements Comparable<Database> {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Database";

    public static final String QUERY_DATABASE_BY_SERVER_PROJECT = "Database.findByServerProject";

    public static final String DATABASE_NAME = "DatabaseName";
    public static final String CONNECTION_NAME = "ConnectionName";

    public static final String DATA_SIZE = "DataSize";
    public static final String LOG_SIZE = "LogSize";
    public static final String RECOVERY_MODE = "RecoveryMode";
    public static final String DELETED = "Deleted";
    public static final String LAST_SYNC_DATE = "Last Sync Date";

    public static final String COMPATIBILITY_LEVEL = "CompatibilityLevel";

    @Column(name="DATABASE_SERVER")
    String databaseServer;

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

    /**
     * @return connection name the database belong to
     * @deprecated use getConnectionName instead
     */
    @Deprecated
    public String getServer() {
        return getConnectionName();
    }
    
    /**
     * @param serverName connection name the database belong to
     * @deprecated use setConnectionName instead
     */
    @Deprecated
    public void setServerName(String serverName) {
        setConnectionName(serverName);
    }

    /**
     * @deprecated use getConnectionName instead
     * @return connection name this database belong to
     */
    @Deprecated
    public String getServerName() {
        return getConnectionName();
    }
    
    public void setConnectionName(String serverName) {
        databaseServer = serverName;
    }

    public String getConnectionName() {
        return databaseServer;
    }

    public String getDatabaseName(){
        return getCustomData(DATABASE_NAME);
    }

    public void setDatabaseName(String databaseName){
        setCustomData(DATABASE_NAME, databaseName);
    }

    @Override
    // TODO (Slava) it's wrong to compare whole object only by server name database
    public int compareTo(Database o) {
        int compare = this.getConnectionName().compareToIgnoreCase(o.getConnectionName());
        if (compare==0) {
            String databaseName = getDatabaseName();
            compare = databaseName.compareToIgnoreCase(o.getDatabaseName());
        }
        return compare;
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
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }
}
