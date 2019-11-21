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
@CustomFieldDiscriminator(Procedure.CUSTOM_FIELD_DISCRIMINATOR)
@DiscriminatorValue("2Proc")
@Cacheable
@BatchSize(size=100)
@FetchAllObjectIdByProjectSql("select mo.id from db_model_object mo "+
        "inner join db_model_datasource m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='2Proc'")
public class Procedure extends ModelObject {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Procedure";
    
    @javax.persistence.Column(name="source")
    @Lob
    String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Parameter> getParameters() {
        if (parameters == null){
            parameters = new ArrayList<Parameter>();
        }
        return listColumnProxy(parameters);
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = mergeColumnList(this.parameters, parameters);
    }

    public Parameter addParameter(Parameter parameter) {
        return addParameter(parameter,-1);
    }
    
    public Parameter addParameter(Parameter parameter, int index) {
        parameters = insert(parameters, parameter, index, "parameter");
        return parameter;
    }
    
    public void removeParameter(Parameter parameter) {
        removeChildColumn(parameters, parameter, "parameter");
    }

    public Parameter getParameter(String paramName) {
        return findByName(parameters, paramName, "paramName");
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
