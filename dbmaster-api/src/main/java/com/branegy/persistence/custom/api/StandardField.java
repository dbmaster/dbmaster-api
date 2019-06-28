package com.branegy.persistence.custom.api;

public final class StandardField{
    private final String mappingName;
    private final String columnName;
    private final boolean nullable;
    private final Type type;
    
    public enum Type{
        TEXT, BOOLEAN, DOUBLE, LONG, DATE
    }
    
    private StandardField(String mappingName, String columnName, boolean nullable, Type type) {
        this.mappingName = mappingName;
        this.columnName = columnName;
        this.nullable = nullable;
        this.type = type;
    }
    
    public static StandardField standard(String columnName, Type type) {
        return new StandardField(null, columnName, false, type);
    }
    
    public static StandardField standard(String mappingName, String columnName, Type type) {
        return new StandardField(mappingName, columnName, false, type);
    }
    
    public static StandardField standardNullable(String columnName, Type type) {
        return new StandardField(null, columnName, true, type);
    }
    
    public static StandardField standardNullable(String mappingName, String columnName, Type type) {
        return new StandardField(mappingName, columnName, true, type);
    }
    
    public final String getMappingName() {
        return mappingName;
    }

    public final String getColumnName() {
        return columnName;
    }

    public final boolean isNullable() {
        return nullable;
    }

    public final Type getType() {
        return type;
    }
}