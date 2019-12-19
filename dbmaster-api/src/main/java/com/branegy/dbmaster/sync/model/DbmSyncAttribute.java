package com.branegy.dbmaster.sync.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType;
import com.branegy.persistence.BaseEntity;

@Entity
@Table(name = "dbm_sync_attribute")
public class DbmSyncAttribute extends BaseEntity {
    
    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="sync_pair_id")
    DbmSyncPair syncPair;

    @Column(name="change_type")
    @Enumerated(EnumType.STRING)
    private AttributeChangeType changeType;

    @Column(name="attribute_name")
    private String attributeName;
    
    @Column(name="source_value")
    private String sourceValue;
    
    @Column(name="target_value")
    private String targetValue;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public AttributeChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(AttributeChangeType changeType) {
        this.changeType = changeType;
    }

    public DbmSyncPair getSyncPair() {
        return syncPair;
    }

    public void setSyncPair(DbmSyncPair syncPair) {
        this.syncPair = syncPair;
    }

}