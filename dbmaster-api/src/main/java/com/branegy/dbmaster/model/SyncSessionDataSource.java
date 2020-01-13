package com.branegy.dbmaster.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.sync.model.DbmSyncSession;
import com.branegy.persistence.IEntity;

@Entity
@Table(name = "DB_SYNCSESSION_DATASOURCE")
@Cache(usage=CacheConcurrencyStrategy.READ_ONLY)
@SqlResultSetMapping(
        name = SyncSessionDataSource.SYNC_SESSION_DATA_SOURCE_MAPPING,
        entities = {
            @EntityResult(
                    entityClass = SyncSessionDataSource.class,
                    fields = {
                            @FieldResult(name = "syncSessionId", column = "SYNCSESSION_ID"),
                            @FieldResult(name = "dataSource", column = "DATASOURCE_ID")
                    }
            ),
            @EntityResult(
                    entityClass = DbmSyncSession.class,
                    fields = {
                        @FieldResult(name = "id",          column = "SS_ID"),
                        @FieldResult(name = "sessionType", column = "SS_SESSION_TYPE"),
                        @FieldResult(name = "project",     column = "SS_PROJECT_ID"),
                        @FieldResult(name = "createAuthor",column = "SS_CREATEAUTHOR"),
                        @FieldResult(name = "created",     column = "SS_CREATED"),
                        @FieldResult(name = "updateAuthor",column = "SS_UPDATEAUTHOR"),
                        @FieldResult(name = "updated",     column = "SS_UPDATED"),
                        @FieldResult(name = "rootPair",    column = "SS_ROOT_PAIR_ID"),
                    }
            ),
            @EntityResult(
                    entityClass = ModelDataSource.class,
                    fields = {
                        @FieldResult(name = "id",          column = "ID"),
                        @FieldResult(name = "createAuthor",column = "CREATEAUTHOR"),
                        @FieldResult(name = "created",     column = "CREATED"),
                        @FieldResult(name = "updateAuthor",column = "UPDATEAUTHOR"),
                        @FieldResult(name = "updated",     column = "UPDATED"),
                        @FieldResult(name = "lastSynch",   column = "LASTSYNCH"),
                        @FieldResult(name = "name",        column = "NAME"),
                        @FieldResult(name = "options.database",    column = "DATABASE"),
                        @FieldResult(name = "options.rawConfig",   column = "CONFIG"),
                        @FieldResult(name = "connection",  column = "CONNECTION_ID"),
                        @FieldResult(name = "order",       column = "ORDER_INDEX"),
                        @FieldResult(name = "model",       column = "MODEL_ID"),
                        @FieldResult(name = "readonly",    column = "READONLY_FLAG"),
                    }
            ),
})  
public class SyncSessionDataSource implements IEntity {
    public static final String SYNC_SESSION_DATA_SOURCE_MAPPING = "SyncSessionDataSourceMapping";

    @Id
    @Column(name="SYNCSESSION_ID",updatable=false,unique = true,nullable = false)
    long syncSessionId;
    
    @OneToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="SYNCSESSION_ID", updatable=false,unique = true,nullable = false)
    DbmSyncSession syncSession;
    
    @ManyToOne(optional = false,fetch=FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="DATASOURCE_ID",updatable=false)
    ModelDataSource dataSource;
    
    public SyncSessionDataSource() {
    }

    public SyncSessionDataSource(ModelDataSource dataSource, DbmSyncSession syncSession) {
        this.dataSource = dataSource;
        this.syncSession = syncSession;
        this.syncSessionId = syncSession.getId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dataSource.hashCode();
        result = prime * result + (int) (syncSessionId ^ (syncSessionId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SyncSessionDataSource other = (SyncSessionDataSource) obj;
        return dataSource.equals(other.dataSource) && syncSessionId == other.syncSession.getId();
    }

    @Override
    public long getId() {
        return syncSessionId;
    }

    public long getSyncSessionId() {
        return syncSessionId;
    }

    public DbmSyncSession getSyncSession() {
        return syncSession;
    }

    public ModelDataSource getDataSource() {
        return dataSource;
    }
   
}
