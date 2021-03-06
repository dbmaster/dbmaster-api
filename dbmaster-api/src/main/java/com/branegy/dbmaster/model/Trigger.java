package com.branegy.dbmaster.model;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.persistence.custom.CustomFieldDiscriminator;

/**
 * Represents trigger
 */

//@Entity
@javax.persistence.Table(name="db_trigger")
@CustomFieldDiscriminator("Trigger")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
/*@FetchAllObjectIdByProjectSql("select c.id from db_constraint c "+
        "inner join db_model_object mo on c.owner_id = mo.id "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId")*/
public class Trigger extends DatabaseObject<ModelObject> {
    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="owner_id")
    // table schema+name
    ModelObject owner;
    
    long triggerId;
    long parentId;
   
    @Column(name="triggerName",length=255)
    @Size(min=1,max=255)
    String triggerName;
    
    String triggerOwner;
    
    
    boolean update;
    boolean delete;
    boolean insert;
    boolean after;
    boolean insteadOf;
    boolean disabled;
   
    @javax.persistence.Column(name="source")
    @Lob
    String source;

    @Override
    public String getName() {
        return triggerName;
    }

    public void setName(String triggerName) {
        this.triggerName = triggerName;
    }

    public ModelObject getOwner() {
        return owner;
    }

    void setOwner(ModelObject owner) {
        this.owner = owner;
    }

    @Override
    final void setParent(ModelObject parent) {
        this.owner = parent;
    }

    @Override
    final ModelObject getParent() {
        return owner;
    }

    public long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(long triggerId) {
        this.triggerId = triggerId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getTriggerOwner() {
        return triggerOwner;
    }

    public void setTriggerOwner(String triggerOwner) {
        this.triggerOwner = triggerOwner;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isInsert() {
        return insert;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    public boolean isAfter() {
        return after;
    }

    public void setAfter(boolean after) {
        this.after = after;
    }

    public boolean isInsteadOf() {
        return insteadOf;
    }

    public void setInsteadOf(boolean insteadOf) {
        this.insteadOf = insteadOf;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
}
