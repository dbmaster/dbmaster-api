package com.branegy.dbmaster.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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
public class SyncSessionDataSource implements IEntity {
    
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
