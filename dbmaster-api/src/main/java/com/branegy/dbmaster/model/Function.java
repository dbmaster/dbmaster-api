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
@CustomFieldDiscriminator("Function")
@DiscriminatorValue("3Func")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@BatchSize(size=100)
@FetchAllObjectIdByProjectSql("select mo.id from db_model_object mo "+
        "inner join db_model m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='3Func'")
public class Function extends ModelObject {
    private static final String TYPE = "Type";
    private static final String EXTRA_INFO = "Extra Info";
    
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
    
    public final String getType(){
        return getCustomData(TYPE);
    }
    
    public final void setType(String type){
        setCustomData(TYPE, type);
    }
    
    public final String getExtraInfo(){
        return getCustomData(EXTRA_INFO);
    }
    
    public final void setExtraInfo(String extraInfo){
        setCustomData(EXTRA_INFO, extraInfo);
    }
}
