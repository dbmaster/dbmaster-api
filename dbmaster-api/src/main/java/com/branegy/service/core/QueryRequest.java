package com.branegy.service.core;

import java.util.ArrayList;
import java.util.List;

import com.branegy.service.core.search.CustomCriterion;
import com.branegy.service.core.search.SearchFilterParser;
import com.branegy.service.core.search.SqlSearchHelper.OrderBy;

/**
 * This object is not thread-safe!
 */
public class QueryRequest {
    private int offset = 0;
    
    /**
     * page size
     */
    private int limit = 0;

    private List<OrderBy> order;
    
    private String filter;
    
    /**
     * explicit list of attributes(relations) separated by comma
     * can be used to limit queries to database when related objects are not required
     * also allows eager initialization of lazy-loaded attributes
     * if not specified - default load will be used
     */
    private String fetchPath;
    
    // converted filters
    private List<CustomCriterion> criterionList = new ArrayList<CustomCriterion>();
    
    public QueryRequest() {
        this(null);
    }

    public QueryRequest(String filter) {
        this.setFilter(filter);
    }

    public QueryRequest(int offset, int limit, OrderBy order, String filter) {
        this.setOffset(offset);
        this.setLimit(limit);
        if (order!=null) {
            addOrderField(order);
        }
        this.setFilter(filter);
    }

    public void addOrderField(OrderBy orderBy) {
        if (order==null) {
            order = new ArrayList<OrderBy>(1);
        }
        order.add(orderBy);
    }

    public List<CustomCriterion> getCriteria() {
        return criterionList;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        criterionList = new ArrayList<CustomCriterion>(SearchFilterParser.parseFilter(filter));
    }

    public String getFetchPath() {
        return fetchPath;
    }

    public void setFetchPath(String fetchPath) {
        this.fetchPath = fetchPath;
    }

    public List<OrderBy> getOrder() {
        return order;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean hasLimit() {
        return limit>0;
    }

    public int getLimit() {
        if (limit == 0) {
            throw new IllegalStateException("Limit is not set, call getLimit() if hasLimit() is true");
        }
        return limit;
    }

    public void setLimit(int limit) {
        if (limit<=0) {
            throw new IllegalArgumentException("Limit must be positive number, or null for unlimit");
        }
        this.limit = limit;
    }
}