package com.branegy.dbmaster.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@javax.persistence.Table(name="db_foreign_key")
@CustomFieldDiscriminator("ForeignKey")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select f.id from db_foreign_key f "+
        "inner join db_model_object mo on f.owner_id = mo.id "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId")
public class ForeignKey extends DatabaseObject<ModelObject> {

    public static enum ReferentialAction {
        // SQL Server Actions
        NoAction, Cascade, SetNull, SetDefault,
        // MySQL additional Action
        Restrict,
        // backup option when action is not recognized
        UnknownAction;
    }
    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="owner_id")
    ModelObject owner;

    @Column(name="name",length=255)
    @Size(min=1,max=255)
    String name;

    @Column(name="disabled")
    boolean disabled;

    @Column(name="deleteAction",length=255)
    @Enumerated(EnumType.STRING)
    ReferentialAction deleteAction;

    @Column(name="updateAction",length=255)
    @Enumerated(EnumType.STRING)
    ReferentialAction updateAction;

    @Column(name="targetTable",length=255)
    @Size(min=1,max=255)
    String targetTable;

    @ElementCollection(fetch=FetchType.EAGER)
    @BatchSize(size=100)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @CollectionTable(name="foreignkey_columns")
    List<ColumnMapping> columns;

    @Embeddable
    public static class ColumnMapping {
        @Column(name="sourceColumnName",length=255)
        @Size(min=1,max=255)
        String sourceColumnName;

        @Column(name="targetColumnName",length=255)
        @Size(min=1,max=255)
        String targetColumnName;

        ColumnMapping(){
        }

        public ColumnMapping(String sourceColumnName, String targetColumnName) {
            this.sourceColumnName = sourceColumnName;
            this.targetColumnName = targetColumnName;
        }

        public String getSourceColumnName() {
            return sourceColumnName;
        }

        public void setSourceColumnName(String sourceColumnName) {
            this.sourceColumnName = sourceColumnName;
        }

        public String getTargetColumnName() {
            return targetColumnName;
        }

        public void setTargetColumnName(String targetColumnName) {
            this.targetColumnName = targetColumnName;
        }

    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public ReferentialAction getDeleteAction() {
        return deleteAction;
    }

    public void setDeleteAction(ReferentialAction deleteAction) {
        this.deleteAction = deleteAction;
    }

    public ReferentialAction getUpdateAction() {
        return updateAction;
    }

    public void setUpdateAction(ReferentialAction updateAction) {
        this.updateAction = updateAction;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public List<ColumnMapping> getColumns() {
        return columns;
    }

    public ColumnMapping addColumnMapping(ColumnMapping mapping){
        if (columns==null){
            columns = new ArrayList<ColumnMapping>();
        }
        columns.add(mapping);
        return mapping;
    }

    public void setColumns(List<ColumnMapping> columns) {
        this.columns = columns;
    }

    void setOwner(ModelObject owner) {
        this.owner = owner;
    }

    public ModelObject getOwner() {
        return owner;
    }

    @Override
    final void setParent(ModelObject parent) {
        this.owner = parent;
    }

    @Override
    final ModelObject getParent() {
        return owner;
    }
}
