package com.branegy.dbmaster.model;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@CustomFieldDiscriminator("Parameter")
@DiscriminatorValue("Parameter")
@Cacheable
@FetchAllObjectIdByProjectSql("select c.id from db_column c "+
        "inner join db_model_object mo on c.owner_id = mo.id "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='Parameter'")
public class Parameter extends Column {

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



}
