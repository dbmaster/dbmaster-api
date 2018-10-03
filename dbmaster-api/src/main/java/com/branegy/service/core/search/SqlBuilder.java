package com.branegy.service.core.search;

public class SqlBuilder{
    private StringBuilder select = new StringBuilder(32);
    private StringBuilder from = new StringBuilder(32);
    private StringBuilder join = new StringBuilder(32);
    private StringBuilder where = new StringBuilder(32);
    private StringBuilder order = new StringBuilder(32);

    public SqlBuilder(String select, String from, String join, String where, String order) {
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
        return where.length()>0;
    }

    boolean hasSelect() {
        return select.length()>0;
    }

    @Override
    public String toString() {
        String result = "SELECT "+select.toString()+"\n  FROM "+from+" ";
        if (join.length()>0){
            result += "\n "+join+" ";
        }
        if (where.length()>0) {
            result += "\n WHERE "+where+" ";
        }
        if (order.length()>0){
            result += "\nORDER BY "+order+" ";
        }
        return result;
    }
    
    @Override
    public final SqlBuilder clone(){
        return new SqlBuilder(select.toString(), from.toString(), join.toString(),
                where.toString(), order.toString());
    }
}