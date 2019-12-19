package com.branegy.dbmaster.sync.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.sync.api.SyncPair.ChangeType;
import com.branegy.persistence.BaseEntity;

@Entity
@Table(name = "dbm_sync_pair")
public class DbmSyncPair extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id")
    DbmSyncSession session;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "source_name",length=255)
    private String sourceName;

    @Column(name = "target_name",length=255)
    private String targetName;
    
    @Column(name = "source_index")
    private Integer sourceIndex;

    @Column(name = "target_index")
    private Integer targetIndex;

    @Column(name = "change_type")
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_pair_id")
    private DbmSyncPair parent;

    @OneToMany(mappedBy = "syncPair", targetEntity = DbmSyncAttribute.class,
               orphanRemoval = true, cascade = CascadeType.ALL)
    @BatchSize(size=300)
    private List<DbmSyncAttribute> attributes;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @BatchSize(size=300)
    private List<DbmSyncPair> childPairs;
    
    @Column(name = "error",length=4096)
    private String error;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public DbmSyncPair getParent() {
        return parent;
    }

    public void setParent(DbmSyncPair parent) {
        this.parent = parent;
    }

    public List<DbmSyncAttribute> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        return attributes;
    }

    public List<DbmSyncPair> getChildPairs() {
        if (childPairs == null) {
            childPairs = new ArrayList<>();
        }
        return childPairs;
    }

    public DbmSyncSession getSession() {
        return session;
    }

    public void setSession(DbmSyncSession session) {
        this.session = session;
    }

    public Integer getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(Integer sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public Integer getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(Integer targetIndex) {
        this.targetIndex = targetIndex;
    }
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
