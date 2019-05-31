package com.branegy.service.core.search;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Parameter;
import javax.persistence.Query;

import com.branegy.dbmaster.core.User;
import com.branegy.service.core.ISecurityContext;
import com.branegy.service.core.search.CustomCriterion.Operator;

public abstract class SqlSearchHelper {
    
    public static final String KEY = "pkey";
    public static final String TEXT = "ptext";
    public static final String INTEGER = "pint";
    public static final String FRACTION = "pfract";
    public static final String TIME = "ptime";
    public static final String BOOL = "pbool";
    public static final String SORT_FIELD = "psortField";

    protected static final List<CustomCriterion> NO_CRITERIA = new ArrayList<CustomCriterion>(0);

    public static class FieldInfo {
        String sqlField;
        String type;
        
        public FieldInfo(String sqlField, String type) {
            this.sqlField = sqlField;
            this.type = type;
        }
    }
    
    protected boolean emptyResult;
    private boolean customEntity;

    /* key in criteria <-> sql path
     * TODO (Vitaly) worked for text field only */
    protected Map<String, FieldInfo> standardFields = Collections.<String, FieldInfo>emptyMap();

    protected List<OrderBy> sortOrder;
    protected String alias;

    protected List<CustomCriterion> criteria;
    
    protected void setCustomEntity(boolean customEntity) {
        this.customEntity = customEntity;
        if (!customEntity) {
            Iterator<CustomCriterion> it = criteria.iterator();
            while (it.hasNext()) {
                CustomCriterion cr = it.next();
                if (!cr.isFreeTextSearch() && !standardFields.containsKey(cr.getKey())) {
                    it.remove();
                    emptyResult = true;
                }
            }
        }
    }
    
    protected void handleContactRelation(SqlBuilder sql, String contactRelationField) {
        for (CustomCriterion cc : criteria) {
           if (cc.getRelation()!=null && cc.getRelation().equals("contact")) {
               String originalAlias = alias;
               alias = "cc";
               SqlBuilder builder1;
               builder1 = new SqlBuilder("cc.id", "inv_contact cc", null, "cc.project_id=:projectId", null);
               generateSQL(builder1, "contact");
               
               alias = "link";
               SqlBuilder builder2;
               builder2 = new SqlBuilder("link."+contactRelationField,"inv_contact_link link",null,null,null);
               generateSQL(builder2, "contact");
               builder2.appendWhere("OR link.contact_id IN (" + builder1 +")");

               sql.appendWhere(" AND "+originalAlias+".id IN ("+ builder2 + ")");
//                      " SELECT link."+contactRelationField+
  //                    " FROM inv_contact_link link WHERE link.contact_id IN (" + builder +") )"
    //           );
               alias = originalAlias;
               break;
           }
        }
    }

    public static void setContext(ISecurityContext context, List<CustomCriterion> criteria) {
        for (int i=0;i<criteria.size();i++) {
            CustomCriterion cc = criteria.get(i);
            if (cc.getRelation()!=null && cc.getRelation().equals("contact")) {
                if (cc.getValue().text!=null && cc.getValue().text.equals("me")) {
                    User user = context.getCurrentUser();
                    String displayName = user.getFirstName()+" "+user.getLastName();
                    cc.setValue(displayName);
                }
            }
        }
    }

    public static class OrderBy {
        private final String field;
        private final SortDir direction;
        private final boolean nullsLast;

        public static enum SortDir {
            ASC, DESC
        }

        public OrderBy(String field, SortDir direction) {
            this(field, direction, direction == SortDir.DESC);
        }
        
        public OrderBy(String field, SortDir direction, boolean nullsLast) {
            if (field == null || direction == null) {
                throw new NullPointerException();
            }
            this.field = field;
            this.direction = direction;
            this.nullsLast = nullsLast;
        }

        public String getField() {
            return field;
        }

        public SortDir getDirection() {
            return direction;
        }

        public boolean isNullsLast() {
            return nullsLast;
        }
        
        private String getDirectionWithNulls(){
            return direction + " NULLS "+(nullsLast?"LAST":"FIRST");
        }
    }

    public void setStandardFields(Map<String, FieldInfo> standardFields) {
        this.standardFields = standardFields;
    }

    private static String buildCustomCriteria(CustomCriterion criterion, int index, boolean freeText) {
        StringBuilder sb = new StringBuilder(128);
        Operator op = criterion.getOperator();
        
        CustomCriterion.ParsedValue value = criterion.getValue();
        if (value.text!=null) {
            if (!criterion.isStrictText() && op==Operator.EQ) {
                sb.append("upper(m"+index+".text) like upper('%'+:"+TEXT+index+"+'%') escape '!' or ");
            } else if (!criterion.isStrictText() && op==Operator.NEQ) {    
                sb.append("upper(m"+index+".text) not like upper('%'+:"+TEXT+index+"+'%') escape '!' or ");
            } else {
                sb.append("upper(m").append(index).append(".text) ")
                  .append(op).append("upper(:"+TEXT+index+") or ");
            }
        }

        if (value.bool!=null) {
            sb.append("m"+index+".bool "+op+" :"+BOOL+index+" or ");
        }
        if (value.fraction!=null) {
            sb.append("m"+index+".fractional "+op+" :"+FRACTION+index+" or ");
        }
        if (value.longValue!=null) {
            sb.append("m"+index+".integer "+op+" :"+INTEGER+index+" or ");
        }
        if (value.date!=null) {
            sb.append("m"+index+".time "+op+" :"+TIME+index+" or ");
        }
        sb.delete(sb.length()-3,sb.length());
        return sb.toString();
    }
    
    protected final void generateSQL(SqlBuilder sql) {
        generateSQL(sql, null);
    }

    protected final void generateSQL(SqlBuilder sql, String relation) {
        boolean allSearchAlreadyIncluded = false;

        for (int cIndex=0; cIndex<criteria.size(); ++cIndex) {
            CustomCriterion criterion = criteria.get(cIndex);
            if (criterion==null) {
                continue;
            }

            String cRelation = criterion.getRelation();
            if (!((relation==null && cRelation==null) ||
                  (relation!=null && cRelation!=null && relation.equals(cRelation)))) {
                continue;
            }

            if (criterion.isFreeTextSearch()) { // search anywhere
                if (allSearchAlreadyIncluded){
                    throw new IllegalStateException("More then one FreeText Search is not allowed");
                } else {
                    allSearchAlreadyIncluded = true;
                    if (customEntity) {
                        sql.appendJoin("LEFT JOIN CUSTOMFIELDENTITY_MAP m"+cIndex+" ON " +
                                "m"+cIndex+".CUSTOMFIELDENTITY_ID="+alias+".CUSTOM_ID");
                        if (sql.hasWhere()){
                            sql.appendWhere("and");
                        }
                        sql.appendWhere("(");
                        sql.appendWhere(buildCustomCriteria(criterion, cIndex, true));
                        // append additional or clause for
                        if (relation==null) {
                            String orClause = addStandardFields(criterion, cIndex);
                            if (orClause!=null) {
                                sql.appendWhere(orClause);
                            }
                        }
                        sql.appendWhere(")");
                    } else if (relation==null) {
                        if (sql.hasWhere()){
                            sql.appendWhere("AND");
                        }
                        sql.appendWhere("(");
                        sql.appendWhere("FALSE");
                        String orClause = addStandardFields(criterion, cIndex);
                        if (orClause!=null) {
                            sql.appendWhere(orClause);
                        }
                        sql.appendWhere(")");
                    }
                }
            } else if (criterion.isPair()) {// key operator value
                if (sql.hasWhere()) {
                    sql.appendWhere(" and ");
                }

                FieldInfo stdField = standardFields.get(criterion.getKey());
                if (stdField==null) {
                    sql.appendJoin("INNER JOIN CUSTOMFIELDENTITY_MAP m"+cIndex+" ON " +
                            "m"+cIndex+".CUSTOMFIELDENTITY_ID="+alias+".CUSTOM_ID");
                    sql.appendWhere("(");
                    // key = value only

                    if (criterion.isStrictKey()){
                        sql.appendWhere("UPPER(m"+cIndex+".map_key) = UPPER(:"+KEY+cIndex+")");
                    } else {
                        sql.appendWhere("UPPER(m"+cIndex+".map_key) LIKE UPPER(:"+KEY+cIndex+") ESCAPE '!'");
                    }
                    sql.appendWhere("AND (");
                    sql.appendWhere(buildCustomCriteria(criterion, cIndex, false));
                    sql.appendWhere("))");
                } else {
                    String where;
                    if (stdField.type.equals(TEXT)) {
                        where = "UPPER("+stdField.sqlField+")";
                        if (!criterion.isStrictText() && criterion.getOperator()==Operator.EQ) {
                            where+=" LIKE UPPER('%'+:"+TEXT+cIndex+"+'%') escape '!' ";
                        } else if (!criterion.isStrictText() && criterion.getOperator()==Operator.NEQ) {
                            where+=" NOT LIKE UPPER('%'+:"+TEXT+cIndex+"+'%') escape '!' ";
                        } else {
                            where+=criterion.getOperator()+"UPPER(:"+TEXT+cIndex+")";
                        }
                        
                    } else if (stdField.type.equals(TIME)) {
                        where = "CAST("+stdField.sqlField+" as date)"+criterion.getOperator()
                               +"CAST(:"+stdField.type+cIndex+" as date)";
                    } else {
                        where = stdField.sqlField+criterion.getOperator()+":"+stdField.type+cIndex;
                    }
                    sql.appendWhere(where);
                }
            } else if (criterion.isEmptyValue()) {// key =
                if (sql.hasWhere()){
                    sql.appendWhere("and");
                }

                FieldInfo stdField = standardFields.get(criterion.getKey());
                if (stdField==null) {
                    sql.appendWhere("("+
                            alias+".custom_id "+
                            (criterion.getOperator()==Operator.EQ ? "not" : "")+
                            " in ("+
                            "SELECT m"+cIndex+".CUSTOMFIELDENTITY_ID " +
                            "from CUSTOMFIELDENTITY_MAP m"+cIndex+" " +
                            "where upper(m"+cIndex+".map_key) like upper(:"+KEY+cIndex+")" +
                            ")"+
                                (criterion.getOperator()==Operator.EQ?" or "+ alias+".custom_id is null":"") +
                            ")"
                            );
                } else {
                    sql.appendWhere(stdField.sqlField+ " IS NULL");
                }
            }
        }
        if (emptyResult) {
            if (sql.hasWhere()){
                sql.appendWhere("and");
            }
            sql.appendWhere("false");
        }
        if (relation==null) {
            setSortOrder(sql);
        }
    }

    private void setSortOrder(SqlBuilder sql) {
        if (sortOrder != null) {
            for (int i = 0; i < sortOrder.size(); i++) {
                OrderBy order = sortOrder.get(i);
                FieldInfo sortField = standardFields.get(order.getField());
                if (sortField == null) {
                    sql.appendJoin("left join CUSTOMFIELDENTITY_MAP SORT" + i + " on" + " SORT" + i
                            + ".CUSTOMFIELDENTITY_ID=" + alias + ".CUSTOM_ID and UPPER(SORT" + i
                            + ".MAP_KEY)=UPPER(:" + SORT_FIELD + i + ")");

                    sql.appendOrder("UPPER(SORT" + i + ".text) " + order.getDirectionWithNulls());
                    sql.appendOrder("SORT" + i + ".bool " + order.getDirectionWithNulls());
                    sql.appendOrder("SORT" + i + ".fractional " + order.getDirectionWithNulls());
                    sql.appendOrder("SORT" + i + ".integer " + order.getDirectionWithNulls());
                    sql.appendOrder("SORT" + i + ".time " + order.getDirectionWithNulls());

                    sql.appendSelect("sort" + i + ".text");
                    sql.appendSelect("sort" + i + ".bool");
                    sql.appendSelect("sort" + i + ".fractional");
                    sql.appendSelect("sort" + i + ".integer");
                    sql.appendSelect("sort" + i + ".time");

                } else {
                    if (sortField.type.equals(TEXT)) {
                        sql.appendOrder("UPPER(" + sortField.sqlField + ") " + order.getDirectionWithNulls());
                    } else  {
                        sql.appendOrder(sortField.sqlField + " " + order.getDirectionWithNulls());
                    }
                }
            }
            // TODO (Slava) We don't need to select, optimize join and select
        }
    }

    /*
     * Set values for the parameters in the query
     */
    protected void setupParameters(Query query) {
        Pattern pattern =
            Pattern.compile("("+KEY+"|"+TEXT+"|"+FRACTION+"|"+INTEGER+"|"+TIME+"|"
                       +SORT_FIELD+"|"+BOOL+")([0-9]+)",Pattern.CASE_INSENSITIVE);
        for (Parameter<?> p:query.getParameters()){
            String name = p.getName();
            Matcher m = pattern.matcher(name);
            // skipping parameters that are not set by SqlSearchHelper
            if (!m.matches()) {
                continue;
            }
            String simpleName = m.group(1);
            int index = Integer.parseInt(m.group(2));
            if (!simpleName.equals(SORT_FIELD) && (criteria==null || criteria.size()<=index)) {
                throw new IllegalArgumentException("Illegal parameter index "+name);
            }
            if (simpleName.equals(SORT_FIELD) && (sortOrder==null || sortOrder.size()<=index)) {
                throw new IllegalArgumentException("Illegal sort index "+name);
            }
            if (SORT_FIELD.equals(simpleName)) {
                query.setParameter(name, sortOrder.get(index).getField());
            } else if (KEY.equals(simpleName)){
                CustomCriterion criterion = criteria.get(index);
                if (criterion.isStrictKey()) {
                    query.setParameter(name, criterion.getKey().toUpperCase());
                } else {
                    query.setParameter(name, '%'+criterion.getKey().toUpperCase()+'%');
                }
            } else if (TEXT.equals(simpleName)){
                CustomCriterion criterion = criteria.get(index);
                query.setParameter(name, criterion.getValue().text.toUpperCase());
            } else if (INTEGER.equals(simpleName)){
                CustomCriterion criterion = criteria.get(index);
                query.setParameter(name, criterion.getValue().longValue);
            } else if (FRACTION.equals(simpleName)){
                CustomCriterion criterion = criteria.get(index);
                query.setParameter(name, criterion.getValue().fraction);
            } else if (TIME.equals(simpleName)){
                CustomCriterion criterion = criteria.get(index);
                query.setParameter(name, criterion.getValue().date);
            } else if (BOOL.equals(simpleName)){
                CustomCriterion criterion = criteria.get(index);
                query.setParameter(name, criterion.getValue().bool);
            }
        }
    }


    private final String addStandardFields(CustomCriterion criterion, int criterionIndex) {
        StringBuffer sb = new StringBuffer();
        for (FieldInfo field: standardFields.values()) {
            sb.append(" OR ");
            sb.append(parameter(field, criterion, criterionIndex));
        }
        return sb.toString();
    }

    private String parameter(FieldInfo stdField, CustomCriterion criterion, int criterionIndex) {
        if (stdField.type.equals(TEXT)) {
            String text = "UPPER("+stdField.sqlField+") LIKE UPPER(%s) ESCAPE '!'";
            return String.format(text, criterion.isStrictText() ? ":"+TEXT + criterionIndex
                                          : "'%'+:"+TEXT + criterionIndex+"+'%'");
        } else if (stdField.type.equals(TIME) ) {
             String text = "cast("+stdField.sqlField+" as date)=cast(%s as date)";
             return String.format(text,  ":"+stdField.type + criterionIndex);
        } else {
            String text = stdField.sqlField+"=%s";
            return String.format(text,  ":"+stdField.type + criterionIndex);
        }
    }

}
