package com.branegy.inventory.model;

import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_BY_APPLICATION;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_BY_SERVER;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_COUNT_BY_APPLICATION;
import static com.branegy.inventory.model.Installation.QUERY_INSTALLATION_COUNT_BY_SERVER;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
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
                + "where p.server.id=:server_id")
//        @NamedQuery(name = QUERY_INSTALLATION_BY_DATABASE, query = "from Installation p "
//                + "where p.id in (select du.installation.id from DatabaseUsage du "
//                + "where du.database.id=:database_id)"),
//        @NamedQuery(name = QUERY_INSTALLATION_COUNT_BY_DATABASE,
//            query = "select count(p) from Installation p "
//                + "where p.id in (select du.installation.id from DatabaseUsage du "
//                + "where du.database.id=:database_id)")
})
@Table(name="inv_app_instance")
// TODO Replace discriminator with Installation
@CustomFieldDiscriminator("Installation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_app_instance ai " +
        "inner join inv_server s on ai.server_id=s.id where s.project_id=:projectId")
public class Installation extends BaseCustomEntity {
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
}
