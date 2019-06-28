package com.branegy.dbmaster.model;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@CustomFieldDiscriminator(View.CUSTOM_FIELD_DISCRIMINATOR)
@DiscriminatorValue("1View")
@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@BatchSize(size=100)
@FetchAllObjectIdByProjectSql("select mo.id from db_model_object mo "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='1View'")
public class View extends ModelObject {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "View";
    
    @javax.persistence.Column(name="source")
    @Lob
    String source;

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

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
    
    public Index addIndex(Index index) {
        this.indexes = addChild(indexes, index, "index");
        return index;
    }
    
    public void removeIndex(Index index) {
        removeChild(indexes, index, "index");
    }

    public Column getColumn(String columnName) {
        return findByName(columns, columnName, "columnName");
    }
    
    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }
}
