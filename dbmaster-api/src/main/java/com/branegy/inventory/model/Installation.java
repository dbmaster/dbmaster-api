package com.branegy.inventory.model;

import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_BY_APPLICATION;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_BY_PROJECT;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_BY_SERVER;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_COUNT_BY_APPLICATION;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_COUNT_BY_SERVER;
import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Access(AccessType.FIELD)
@NamedQueries( {
        @NamedQuery(name = QUERY_INSTALLATION_BY_APPLICATION, query = "from Installation p "
                + "where p.application.id=:application_id"),
        @NamedQuery(name = QUERY_INSTALLATION_COUNT_BY_APPLICATION,
            query = "select count(p) from Installation p "
                + "where p.application.id=:application_id"),
        @NamedQuery(name = QUERY_INSTALLATION_BY_SERVER, query = "from Installation p "
                + "where p.server.id=:server_id"),
        @NamedQuery(name = QUERY_INSTALLATION_COUNT_BY_SERVER,
            query = "select count(p) from Installation p "
                + "where p.server.id=:server_id"),
        @NamedQuery(name = QUERY_INSTALLATION_BY_PROJECT, query = "from Installation i "
                + "where i.application.project.id=:projectId"),
        
        
//        @NamedQuery(name = QUERY_INSTALLATION_BY_DATABASE, query = "from Installation p "
//                + "where p.id in (select du.installation.id from DatabaseUsage du "
//                + "where du.database.id=:database_id)"),
//        @NamedQuery(name = QUERY_INSTALLATION_COUNT_BY_DATABASE,
//            query = "select count(p) from Installation p "
//                + "where p.id in (select du.installation.id from DatabaseUsage du "
//                + "where du.database.id=:database_id)")
})
@Table(name="inv_app_instance")
@CustomFieldDiscriminator(Installation.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_app_instance ai " +
        "inner join inv_server s on ai.server_id=s.id where s.project_id=:projectId")
public class Installation extends BaseCustomEntity {
    public static final String CUSTOM_FIELD_DISCRIMINATOR = "Installation";
    //    public static final String QUERY_INSTALLATION_BY_DATABASE =
//            "Installation.findByDatabase";
//    public static final String QUERY_INSTALLATION_COUNT_BY_DATABASE =
//            "Installation.findCountByDatabase";
    public static final String QUERY_INSTALLATION_BY_SERVER =
            "Installation.findByServer";
    public static final String QUERY_INSTALLATION_COUNT_BY_SERVER =
            "Installation.findCountByServer";
    public static final String QUERY_INSTALLATION_BY_APPLICATION =
            "Installation.findByApplication";
    public static final String QUERY_INSTALLATION_COUNT_BY_APPLICATION =
            "Installation.findCountByApplication";
    public static final String QUERY_INSTALLATION_BY_PROJECT =
            "Installation.findByProject";

    public static final String INSTANCE_NAME = "InstanceName";
    public static final String LAST_SYNC_DATE = "Last Sync Date";

    @ManyToOne(optional=false)
    @JoinColumn(name="server_id")
    Server server;

    @ManyToOne(optional=false)
    @JoinColumn(name="application_id")
    Application application;

    @Column(name="lastSynch",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    Date lastSynch;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Date getLastSynch() {
        return lastSynch;
    }

    public void setLastSynch(Date lastSynch) {
        this.lastSynch = lastSynch;
    }

    public String getTitle() {
        String title = getServer().getCustomData(Server.SERVER_NAME);
        String instanceName = getCustomData(INSTANCE_NAME);
        if (instanceName!=null && instanceName.trim().length()>0) {
            title+="\\"+instanceName;
        }
        return title;
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
