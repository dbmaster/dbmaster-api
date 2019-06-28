package com.branegy.persistence.custom;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmbeddableKey implements Comparable<EmbeddableKey> {
    public static final String ENTITY_ID_COLUMN = "ENTITY_ID";
    public static final String CLAZZ_COLUMN     = "ENTITY_TYPE";
    public static final String KEY_COLUMN       = "FIELD_NAME";
    public static final String ORDER_COLUMN     = "VALUE_ORDER";
    
    @Column(name = CLAZZ_COLUMN, nullable = false, updatable = false)
    private String entityType;
    
    @Column(name = KEY_COLUMN, nullable = false)
    private String fieldName;

    @Column(name = ORDER_COLUMN)
    private int valueOrder;
    
    EmbeddableKey() {
    }

    public EmbeddableKey(String map_key, int order_index, String clazz) {
        this.fieldName = map_key;
        this.valueOrder = order_index;
        this.entityType = clazz;
    }

    @Override
    public String toString() {
        return "[fieldName=" + fieldName + ", valueOrder=" + valueOrder + ", entityType=" + entityType
                + "]";
    }

    @Override
    public int compareTo(EmbeddableKey o) {
        int cmp = fieldName.compareTo(o.fieldName);
        if (cmp == 0) {
            cmp = Integer.compare(valueOrder, o.valueOrder);
        }
        return cmp;
    }

    @Override
    public int hashCode() {
        return fieldName.hashCode();
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
        EmbeddableKey other = (EmbeddableKey) obj;
        if (entityType == null) {
            if (other.entityType != null) {
                return false;
            }
        } else if (!entityType.equals(other.entityType)) {
            return false;
        }
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        } else if (!fieldName.equals(other.fieldName)) {
            return false;
        }
        if (valueOrder != other.valueOrder) {
            return false;
        }
        return true;
    }

    public final String getFieldName() {
        return fieldName;
    }

    public final int getValueOrder() {
        return valueOrder;
    }
}
