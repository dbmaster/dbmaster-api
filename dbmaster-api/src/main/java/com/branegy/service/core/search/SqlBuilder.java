package com.branegy.service.core.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SqlBuilder{
    private static final Pattern TRY_FROM_STRING = Pattern.compile(
            "^"+
            "\\s*SELECT\\s+(?<select>.+?)"+
            "\\s+FROM\\s+(?<from>\\S+(?:\\s+\\S+)?)" +
            "(?:\\s+(?<join>.*?JOIN.+?)\\s*)?" + 
            "(?:\\s+WHERE\\s+(?<where>.+?)\\s*)?"+ 
            "(?:\\s+ORDER\\s+BY\\s+(?<order>.+?)\\s*)?"+
            "$", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /**
     * select * from (<any>) as X on ....
     */
    private static final Pattern FROM_AS_ALIAS = Pattern.compile(
            "^(?<temp>\\(.+\\))\\s+AS\\s+(?<alias>[^\\s,(){}]+)$", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private final StringBuilder select = new StringBuilder(32);
    private final StringBuilder from   = new StringBuilder(32);
    private final StringBuilder join   = new StringBuilder(32);
    private final StringBuilder where  = new StringBuilder(32);
    private final StringBuilder order  = new StringBuilder(32);
    private final StringBuilder suffix = new StringBuilder(32);
    
    
    public static SqlBuilder selectFrom(String select, String from) {
        return new SqlBuilder(select, from);
    }
    
    public SqlBuilder(String select, String from) {
        this(select, from, null, null, null, null);
    }
    
    public SqlBuilder(String select, String from, String join, String where, String order) {
        this(select, from, join, where, order, null);
    }

    public SqlBuilder(String select, String from, String join, String where, String order, String suffix) {
        if (select!=null){
            this.select.append(select);
        }
        if (from!=null){
            this.from.append(from);
        }
        if (join!=null){
            this.join.append(join);
        }
        if (where!=null){
            this.where.append(where);
        }
        if (order!=null){
            this.order.append(order);
        }
        if (suffix!=null) {
            this.suffix.append(suffix);
        }
    }
    
    public void appendSelect(String select) {
        if (this.select.length()>0){
            this.select.append(',');
        }
        this.select.append(' ');
        this.select.append(select);
    }

    public void appendJoin(String join) {
        if (!join.startsWith(" ")){
            this.join.append(' ');
        }
        this.join.append(join);
    }

    public void appendAndWhere(String where) {
        if (hasWhere()) {
            this.where.append(" AND ");
        } else if (!where.startsWith(" ")) {
            this.where.append(' ');
        }
        this.where.append(where);
    }
    
    public void appendWhere(String where) {
        if (!where.startsWith(" ")) {
            this.where.append(' ');
        }
        this.where.append(where);
    }

    public void appendOrder(String order) {
        String trim = this.order.toString().trim();
        if (trim.length()>0 && !trim.endsWith(",")) {
            this.order.append(", ");
        }
        this.order.append(order);
    }

    public boolean hasWhere() {
        return where.length()!=0;
    }
    
    public boolean hasOrderBy() {
        return order.length()!=0;
    }
    
    public boolean isDistinct() {
        return select.toString().toLowerCase().startsWith("distinct ");
    }

    public SqlBuilder setSelect(String select) {
        this.select.setLength(0);
        this.select.append(select);
        return this;
    }

    public void setSuffix(String suffix) {
        this.suffix.setLength(0);
        this.suffix.append(suffix);
    }
    
    public String getTableAlias() {
        String from = getFrom();
        String[] args = from.split("\\s+");
        if (args.length==1) {
            return args[0];
        } else if (args.length==2) {
            return args[1];
        } else {
            Matcher matcher = FROM_AS_ALIAS.matcher(from);
            if (matcher.matches()) {
                return matcher.group("alias");
            }
            throw new IllegalStateException("Can't detect table alias");
        }
    }
    
    public String getFrom() {
        return from.toString().trim();
    }
    
    @Override
    public String toString() {
        String result = "SELECT "+select.toString()+"\nFROM "+from+" ";
        if (join.length()>0){
            result += "\n"+join+" ";
        }
        if (hasWhere()) {
            result += "\nWHERE "+where+" ";
        }
        if (order.length()>0){
            result += "\nORDER BY "+order+" ";
        }
        if (suffix.length()>0){
            result += "\n"+suffix;
        }
        return result;
    }
    
    @Override
    public final SqlBuilder clone(){
        return new SqlBuilder(select.toString(), from.toString(), join.toString(),
                where.toString(), order.toString(), suffix.toString());
    }

    public static SqlBuilder tryFromString(String sql) {
        Matcher matcher = TRY_FROM_STRING.matcher(sql);
        if (matcher.matches()){
            
            String select = matcher.group("select");
            String from = matcher.group("from");
            String join = matcher.group("join");
            String where = matcher.group("where");
            String order = matcher.group("order");
            
            return new SqlBuilder(select, from, join, where, order, null);
        }
        throw new IllegalArgumentException("Can't parse sql");
    }
}