package com.branegy.dbmaster.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@CustomFieldDiscriminator("View")
@DiscriminatorValue("1View")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@BatchSize(size=100)
@FetchAllObjectIdByProjectSql("select mo.id from db_model_object mo "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='1View'")
public class View extends ModelObject {
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
}
