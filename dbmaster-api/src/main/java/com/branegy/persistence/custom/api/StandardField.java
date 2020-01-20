package com.branegy.persistence.custom.api;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.branegy.service.core.exception.IllegalArgumentApiException;

public final class StandardField{
    private static final Pattern AS_PATTERN = Pattern.compile("^(?:"
                                                                + "(?:(.+)\\s+AS\\s+(\\S+))"
                                                                    + "|"
                                                                + "(?:[^.()]+\\.([^.()]+))"
                                                                    + "|"
                                                                + "([^.()]+)"
                                                             + ")$",CASE_INSENSITIVE | DOTALL);
    private final String mappingName;
    
    private final String columnExpression;
    private final String alias;
    
    private final String sortJoinForUnion;
    private final String sortJoinAliasForUnion;
    
    private final boolean nullable;
    private final Type type;
    
    public enum Type{
        TEXT, BOOLEAN, DOUBLE, LONG, DATE
    }
    
    /**
     * mappingName - business name
     * columnExpression - name, alias.name, expression as alias
     * 
     */
    private StandardField(String mappingName, String columnExpression, 
            String sortJoinForUnion, 
            String sortJoinAliasForUnion, boolean nullable, Type type) {
        this.mappingName = mappingName;
        Matcher matcher = AS_PATTERN.matcher(columnExpression);
        if (matcher.matches()) {
            if (matcher.group(1)!=null) {
                this.alias = matcher.group(2);
                this.columnExpression = matcher.group(1);
            } else if (matcher.group(3)!=null) {
                this.alias = matcher.group(3);
                this.columnExpression = columnExpression;
            } else {
                this.alias = matcher.group(4);
                this.columnExpression = columnExpression;
            }
        } else {
            this.alias = null;
            this.columnExpression = columnExpression;
        }
        if ((sortJoinForUnion == null) != (sortJoinAliasForUnion == null)) {
            throw new IllegalArgumentApiException("sortJoin nulls or not nulls");
        }
        this.sortJoinForUnion = sortJoinForUnion;
        this.sortJoinAliasForUnion = sortJoinAliasForUnion;
        this.nullable = nullable;
        this.type = type;
    }
    
    public static StandardField standard(String columnName, Type type) {
        return new StandardField(null, columnName, null, null, false, type);
    }
    
    public static StandardField standard(String mappingName, String columnName, Type type) {
        return standard(mappingName, columnName, null, null, type);
    }
    
    public static StandardField standard(String mappingName, String columnExpression, 
            String sortJoinForUnion, String sortJoinAliasForUnion, Type type) {
        return new StandardField(mappingName, columnExpression, sortJoinForUnion, 
                sortJoinAliasForUnion, false, type);
    }
    
    public static StandardField standardNullable(String columnName, Type type) {
        return new StandardField(null, columnName, null, null,  true, type);
    }
    
    public static StandardField standardNullable(String mappingName, String columnName, Type type) {
        return standardNullable(mappingName, columnName, null, null, type);
    }
    
    public static StandardField standardNullable(String mappingName, String columnExpression,
            String sortJoinForUnion,
            String sortJoinAliasForUnion, Type type) {
        return new StandardField(mappingName, columnExpression, sortJoinForUnion, sortJoinAliasForUnion,
                true, type);
    }
    
    public final String getMappingName() {
        return mappingName;
    }

    
    /**
     * @return column expression
     */
    public final String getColumnExpression() {
        return columnExpression;
    }
    
    /**
     * @return alias only
     */
    public final String getAlias() {
        if (alias == null) {
            throw new IllegalArgumentApiException("alias is emtpy for "+columnExpression);
        }
        return alias;
    }

    public final boolean isNullable() {
        return nullable;
    }

    public final Type getType() {
        return type;
    }

    public String getSortJoinForUnion() {
        return sortJoinForUnion;
    }

    public String getSortJoinAliasForUnion() {
        return sortJoinAliasForUnion;
    }
}