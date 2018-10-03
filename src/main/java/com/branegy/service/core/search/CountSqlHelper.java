package com.branegy.service.core.search;

import static com.branegy.service.core.helper.BaseServiceImpl.disableInvalidateCache;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class CountSqlHelper extends SqlSearchHelper {
    protected SqlBuilder sql;

    public CountSqlHelper(SqlBuilder sql, String alias, List<CustomCriterion> criteria) {
        this.sql = sql;
        this.alias = alias;
        this.criteria = criteria !=null ? new ArrayList<CustomCriterion>(criteria) : NO_CRITERIA;
        sql.appendSelect("count(distinct "+alias+".id)");
    }

    public Query getQueryForCustomEntity(EntityManager em) {
        return getQuery(em, null, true);
    }
    
    public Query getQuery(EntityManager em, boolean custom) {
        return getQuery(em, null, custom);
    }
    
    public Query getQuery(EntityManager em, String contactRelationField, boolean custom) {
        setCustomEntity(custom);
        generateSQL(sql);
        
        if (contactRelationField!=null) {
            handleContactRelation(sql, contactRelationField);
        }
        Query query =  em.createNativeQuery(sql.toString());
        
        disableInvalidateCache(query);
        setupParameters(query);
        return query;
    }
    

}
