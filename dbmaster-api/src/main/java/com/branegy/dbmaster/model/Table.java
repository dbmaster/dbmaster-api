package com.branegy.dbmaster.model;

import static com.branegy.persistence.custom.EmbeddableObject.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableObject.ENTITY_ID_COLUMN;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableObject;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@CustomFieldDiscriminator(Table.CUSTOM_FIELD_DISCRIMINATOR)
@DiscriminatorValue("0Tabl")
@Cacheable
@BatchSize(size=100)
@FetchAllObjectIdByProjectSql("select mo.id from db_model_object mo "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='0Tabl'")
public class Table extends ModelObject {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Table";
    
    public static final String DESCRIPTION = "Description";
    public static final String ROWS = "Rows";

    public List<Column> getColumns() {
        if (columns == null){
            columns = new ArrayList<Column>();
        }
        return listColumnProxy(columns);
    }

    public void setColumns(List<Column> columns) {
        this.columns = mergeColumnList(this.columns, columns);
    }

    public List<Index> getIndexes() {
        return unmodifiableList(indexes);
    }

    public void setIndexes(List<Index> indexes) {
        this.indexes = mergeList(this.indexes, indexes);
    }

    public List<Constraint> getConstraints() {
        return unmodifiableList(constraints);
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = mergeList(this.constraints, constraints);
    }

    public List<ForeignKey> getForeignKeys() {
        return unmodifiableList(foreignKeys);
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = mergeList(this.foreignKeys, foreignKeys);
    }

    public Column addColumn(Column column) {
        return addColumn(column,-1);
    }
    
    public Column addColumn(Column column, int index) {
        columns = insert(columns, column, index, "column");
        return column;
    }
    
    public void removeColumn(Column column) {
        removeChildColumn(columns, column, "column");
    }

    public Column getColumn(String columnName) {
        return findByName(columns, columnName, "columnName");
    }
    
    public Index addIndex(Index index) {
        this.indexes = addChild(indexes, index, "index");
        return index;
    }
    
    public void removeIndex(Index index) {
        removeChild(indexes, index, "index");
    }

    public Index getIndex(String indexName) {
        return findByName(indexes, indexName, "indexName");
    }

    public Constraint addConstraint(Constraint constraint) {
        this.constraints = addChild(constraints, constraint, "constraint");
        return constraint;
    }
    
    public void removeConstraint(Constraint constraint) {
        removeChild(constraints, constraint, "constraint");
    }

    public Constraint getConstraint(String constraintName) {
        return findByName(constraints, constraintName, "constraintName");
    }

    public ForeignKey addForeignKey(ForeignKey foreignKey) {
        this.foreignKeys = addChild(foreignKeys, foreignKey, "foreignKey");
        return foreignKey;
    }
    
    public void removeForeignKey(ForeignKey foreignKey) {
        removeChild(foreignKeys, foreignKey, "foreignKey");
    }

    public ForeignKey getForeignKey(String fkName) {
        return findByName(foreignKeys, fkName, "fkName");
    }
    
    public List<Trigger> getTriggers() {
        return unmodifiableList(triggers);
    }
    
    public Trigger addTrigger(Trigger trigger) {
        this.triggers = addChild(triggers, trigger, "trigger");
        return trigger;
    }
    
    public void removeTrigger(Trigger trigger) {
        removeChild(triggers, trigger, "index");
    }

    public Trigger getTrigger(String triggerName) {
        return findByName(triggers, triggerName, "triggerName");
    }
    
    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    protected List<EmbeddableObject> getCustom() {
        return getInnerCustomList();
    }
}
