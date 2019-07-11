package com.branegy.service.core;

import java.util.ArrayList;
import java.util.List;

import com.branegy.persistence.custom.api.OrderBy;
import com.branegy.persistence.custom.api.QueryExpression;
import com.branegy.persistence.custom.api.SqlCustomSearchParserFactory;

/**
 * This object is not thread-safe!
 */
public final class QueryRequest {
    private int offset = 0;
    
    /**
     * page size
     */
    private int limit = 0;
    
    private List<OrderBy> orders;

    /**
     * explicit list of attributes(relations) separated by comma
     * can be used to limit queries to database when related objects are not required
     * also allows eager initialization of lazy-loaded attributes
     * if not specified - default load will be used
     */
    private String fetchPath;
    
    private boolean calculateTotalSize = true;
    
    private QueryExpression criteria;
    
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
            addOrderBy(order);
        }
        this.setFilter(filter);
    }
    
    public QueryRequest(int offset, int limit, OrderBy order, String filter, String... filterExtensions) {
        this.setOffset(offset);
        this.setLimit(limit);
        if (order!=null) {
            addOrderBy(order);
        }
        this.setFilter(filter, filterExtensions);
    }

    public void addOrderBy(OrderBy orderBy) {
        if (orders==null) {
            orders = new ArrayList<>(2);
        }
        orders.add(orderBy);
    }
    
    private void setFilter(String filter, String... filterExtensions) {
        criteria = SqlCustomSearchParserFactory.get().parse(filter,filterExtensions);
    }

    public String getFetchPath() {
        return fetchPath;
    }

    public void setFetchPath(String fetchPath) {
        this.fetchPath = fetchPath;
    }
    
    public boolean hasOrderBy() {
        return orders!=null && !orders.isEmpty();
    }

    public List<OrderBy> getOrderByList() {
        if (orders == null) {
            orders = new ArrayList<>(2);
        }
        return orders;
    }

    public int getOffset() {
        return offset;
    }
    
    public boolean hasOffset() {
        return offset!=0;
    }
    

    public void setOffset(int offset) {
        if (offset<0) {
            throw new IllegalArgumentException("Offset must be positive number");
        }
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

    public boolean isCalculateTotalSize() {
        return calculateTotalSize;
    }

    public void setCalculateTotalSize(boolean calculateTotalSize) {
        this.calculateTotalSize = calculateTotalSize;
    }
    
    public QueryExpression getQueryExpression() {
        return criteria;
    }

    public final void setQueryExpression(QueryExpression queryExpression) {
        this.criteria = queryExpression;
    }
}