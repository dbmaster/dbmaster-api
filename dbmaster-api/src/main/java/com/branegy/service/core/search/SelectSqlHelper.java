package com.branegy.service.core.search;

import static com.branegy.service.core.helper.BaseServiceImpl.disableInvalidateCache;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.branegy.persistence.BaseEntity;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.service.core.QueryRequest;

@Deprecated
public class SelectSqlHelper extends SqlSearchHelper {
    private int offset;
    private int limit;
    protected SqlBuilder sql;


    public SelectSqlHelper(SqlBuilder sql, String alias, QueryRequest request) {
        this.sql = sql;
        this.alias = alias;
/*        this.sortOrder = request.getOrder();
        this.criteria = request.getCriteria() != null
                 ? new ArrayList<CustomCriterion>(request.getCriteria())
                 : NO_CRITERIA;*/
        this.offset = request.getOffset();
        this.limit = request.hasLimit() ? request.getLimit() : 0;
        sql.appendSelect("distinct " + alias + ".*");
    }

    public <T extends BaseEntity> Query getQuery(EntityManager em, Class<T> clazz) {
        return getQuery(em, clazz, null);
    }

    public <T extends BaseEntity> Query getQuery(EntityManager em, Class<T> clazz,
            String contactRelationField) {
        setCustomEntity(BaseCustomEntity.class.isAssignableFrom(clazz));
        generateSQL(sql);

        if (contactRelationField!=null) {
            handleContactRelation(sql, contactRelationField);
        }

        Query query = em.createNativeQuery(sql.toString(), clazz);
        disableInvalidateCache(query);
        setupParameters(query);
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (limit != 0) {
            query.setMaxResults(limit);
        }
        return query;
    }
}
