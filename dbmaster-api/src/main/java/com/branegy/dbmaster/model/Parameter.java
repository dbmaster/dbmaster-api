package com.branegy.dbmaster.model;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@CustomFieldDiscriminator(Parameter.CUSTOM_FIELD_DISCRIMINATOR)
@DiscriminatorValue(Parameter.CUSTOM_FIELD_DISCRIMINATOR)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select c.id from db_column c "+
        "inner join db_model_object mo on c.owner_id = mo.id "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='Parameter'")
public class Parameter extends Column {
    public static final String CUSTOM_FIELD_DISCRIMINATOR = "Parameter";

    public static enum ParamType{
        IN,OUT,IN_OUT
    }

    @Enumerated(EnumType.STRING)
    @javax.persistence.Column(name="paramType",length=255)
    ParamType paramType;

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }

/*    @Override
    public String getKey() {
        return parent.getKey()+"param:"+name+"/";
    }*/

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
