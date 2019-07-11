package com.branegy.persistence.custom.api;

import java.util.Objects;

public class OrderBy {
    private final String field;
    private final boolean asc;
    private final boolean custom;
    private final boolean nullsLast;
    
    public static OrderBy customByAsc(String field) {
        return customBy(field, true, true);
    }
    
    public static OrderBy customByDesc(String field) {
        return customBy(field, false, false);
    }
    
    public static OrderBy customBy(String field, boolean asc) {
        return asc?customByAsc(field):customByDesc(field);
    }
    
    public static OrderBy customBy(String field, boolean asc, boolean nullsLast) {
        return new OrderBy(true, field, asc, nullsLast);
    }
    
    public static OrderBy standardByAsc(String field) {
        return standardBy(field, true, true);
    }
    
    public static OrderBy standardByDesc(String field) {
        return standardBy(field, false, false);
    }
    
    public static OrderBy standardBy(String field, boolean asc) {
        return asc?standardByAsc(field):standardByDesc(field);
    }
    
    public static OrderBy standardBy(String field, boolean asc, boolean nullsLast) {
        return new OrderBy(false, field, asc, nullsLast);
    }
    
    protected OrderBy(boolean custom, String field, boolean asc, boolean nullsLast) {
        Objects.requireNonNull(field);
        this.custom = custom;
        this.field = field;
        this.asc = asc;
        this.nullsLast = nullsLast;
    }

    public String getField() {
        return field;
    }

    public boolean isAsc() {
        return asc;
    }

    public boolean isNullsLast() {
        return nullsLast;
    }
    
    public final boolean isCustomField() {
        return custom;
    }
    
    /**
     * is field contain alias? auto detect with contains(".")
     */
    public final boolean isFullName() {
        return field.indexOf('.')!=-1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(field);
        builder.append(' ');
        if (asc) {
            builder.append("ASC");
        } else {
            builder.append("DESC");
        }
        if (custom) {
            builder.append(" {custom}");
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, asc, nullsLast, custom);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OrderBy other = (OrderBy) obj;
        return asc == other.asc && custom == other.custom && nullsLast == other.nullsLast
                && Objects.equals(field, other.field);
    }
}