package com.branegy.dbmaster.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

/**
 * Represents check constraints.
 * SQL Server: http://msdn.microsoft.com/en-us/library/ms188258.aspx
 * @author slava
 */

@Entity
@javax.persistence.Table(name="db_constraint")
@CustomFieldDiscriminator("Constraint")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select c.id from db_constraint c "+
        "inner join db_model_object mo on c.owner_id = mo.id "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId")
public class Constraint extends DatabaseObject<ModelObject> {
    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="owner_id")
    ModelObject owner;

    @Column(name="constraintName",length=255)
    @Size(min=1,max=255)
    String constraintName;

    /**
     * Can be null (in this case constraint is defined on table level.
     */
    @Column(name="columnName",length=255)
    @Size(min=1,max=255)
    String columnName;

    @Column(name="definition",length=4*1024*1024)
    @Size(min=1,max=4*1024*1024)
    String definition;

    @Column(name="disabled")
    boolean disabled;

    @Override
    public String getName() {
        return constraintName;
    }

    public void setName(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
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
    
}
