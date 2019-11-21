package com.branegy.dbmaster.model;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="db_index")
@CustomFieldDiscriminator(Index.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select i.id from db_index i "+
    "inner join db_model_object mo on i.owner_id = mo.id "+
    "inner join db_model_datasource m on m.id = mo.model_id "+
    "where m.project_id = :projectId")
public class Index extends DatabaseObject<ModelObject>{
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Index";

    public static enum IndexType {
        // All Types related to SQL Server
        Heap, Clustered, Nonclustered, XML, Spatial, UnknownIndexType,
        // MySQL Specific Type
        FullText, BTree, RTree, Hash;
    }

    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="owner_id")
    ModelObject owner;

    @Column(name="name",length=255)
    @Size(min=1,max=255)
    String name;

    @Column(name="type",length=255)
    @Enumerated(EnumType.STRING)
    IndexType type;

    @Column(name="primaryKey",length=255)
    boolean primaryKey;

    @Column(name="index_unique")
    boolean unique;

    @Column(name="ignoreDuplicates")
    boolean ignoreDuplicates;

    @Column(name="disabled")
    boolean disabled;

    @Column(name="fillFactor")
    int fillFactor;

    @Column(name="indexSize")
    Long indexSize; /* (in KB) */

    @Column(name="fragmentation")
    Double fragmentation;

    @ElementCollection(fetch=FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)
    @CollectionTable(name="index_columns")
    List<IndexColumn> columns;

    @Embeddable
    public static class IndexColumn  {
        @Column(name="name",length=255)
        String name;

        @Column(name="asc")
        boolean asc;

        @Column(name="included")
        boolean included;

        public String getColumnName() {
            return name;
        }

        public void setColumnName(String columnName) {
            this.name = columnName;
        }

        public boolean isAsc() {
            return asc;
        }

        public void setAsc(boolean asc) {
            this.asc = asc;
        }

        public boolean isIncluded() {
            return included;
        }

        public void setIncluded(boolean included) {
            this.included = included;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IndexType getType() {
        return type;
    }

    public void setType(IndexType type) {
        this.type = type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public void setIgnoreDuplicates(boolean ignoreDuplicates) {
        this.ignoreDuplicates = ignoreDuplicates;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getFillFactor() {
        return fillFactor;
    }

    public void setFillFactor(int fillFactor) {
        this.fillFactor = fillFactor;
    }

    public Long getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(Long indexSize) {
        this.indexSize = indexSize;
    }

    public Double getFragmentation() {
        return fragmentation;
    }

    public void setFragmentation(Double fragmentation) {
        this.fragmentation = fragmentation;
    }

    public List<IndexColumn> getColumns() {
        return columns;
    }

    public IndexColumn addIndexColumn(IndexColumn indexColumn) {
        if (columns==null){
            columns = new ArrayList<IndexColumn>();
        }
        columns.add(indexColumn);
        return indexColumn;
    }

    public void setColumns(List<IndexColumn> columns) {
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
    
    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }
}
