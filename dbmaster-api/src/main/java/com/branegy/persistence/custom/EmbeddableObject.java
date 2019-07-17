package com.branegy.persistence.custom;

import static com.branegy.persistence.BaseEntity.UPDATE_AUTHOR_LENGTH;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.branegy.persistence.CurrentUserService;

@Embeddable
public class EmbeddableObject implements Comparable<EmbeddableObject> {
    private static final Date unknown = new Date(0);
    
    public static final String ENTITY_ID_COLUMN = "ENTITY_ID";
    public static final String CLAZZ_COLUMN     = "ENTITY_TYPE";
    public static final String KEY_COLUMN       = "FIELD_NAME";
    public static final String ORDER_COLUMN     = "VALUE_ORDER";
    
    public static final String BOOLEAN_COLUMN = "FLAG_VALUE";
    public static final String DATE_COLUMN    = "TIME_VALUE";
    public static final String DOUBLE_COLUMN  = "FLOAT_VALUE";
    public static final String LONG_COLUMN    = "INT_VALUE";
    public static final String TEXT_COLUMN    = "TEXT_VALUE";
    
    @Column(name = CLAZZ_COLUMN, nullable = false, updatable = false)
    private String entityType;
    
    @Column(name = KEY_COLUMN, nullable = false)
    private String fieldName;

    @Column(name = ORDER_COLUMN)
    // min = 0
    // max = 2^15-1
    private int valueOrder;
    
    
    //==========
    @Column(name = TEXT_COLUMN, length=4*1024*1024)
    private String text;
    
    @Column(name = LONG_COLUMN)
    private Long longValue;
    
    @Column(name = DOUBLE_COLUMN)
    private Double doubleValue;
    
    @Column(name = BOOLEAN_COLUMN)
    private Boolean booleanValue;
    
    @Column(name = DATE_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="UPDATED_AT",nullable = false)
    private Date updatedAt = unknown;

    @Column(name="UPDATED_BY",length = UPDATE_AUTHOR_LENGTH)
    private String updatedBy;
    
    
    EmbeddableObject() {
    }

    public EmbeddableObject(String fieldName, int valueOrder, String entityType, Object object) {
        this.fieldName = fieldName;
        this.valueOrder = valueOrder;
        this.entityType = entityType;
        setObject(object);
    }

    @Override
    public int compareTo(EmbeddableObject o) {
        int cmp = fieldName.compareTo(o.fieldName);
        if (cmp == 0) {
            cmp = valueOrder-o.valueOrder;
        }
        return cmp;
    }

    public final String getFieldName() {
        return fieldName;
    }

    public final int getValueOrder() {
        return valueOrder;
    }

    public EmbeddableObject(Object object) {
        setObject(object);
    }
    
    @PrePersist
    @PreUpdate
    protected void preUpdate() {
        updatedBy = CurrentUserService.getCurrentUser(UPDATE_AUTHOR_LENGTH);
        updatedAt = new Date();
    }

    public Object getObject() {
        if (text != null) {
            return text;
        } else if (longValue != null) {
            return longValue;
        } else if (doubleValue != null) {
            return doubleValue;
        } else if (booleanValue != null) {
            return booleanValue;
        } else if (date != null) {
            return date;
        } else {
            throw new IllegalArgumentException("All fields can't be null");
        }
    }

    // TODO hide!
    public void setObject(Object object) {
        if (object instanceof String) {
            text = (String) object;
            longValue = null;
            doubleValue = null;
            booleanValue = null;
            date = null;
        } else if (object instanceof Number) {
            if (object instanceof Double || object instanceof Float) {
                text = null;
                longValue = null;
                doubleValue = ((Number) object).doubleValue();
                booleanValue = null;
                date = null;
            } else {
                text = null;
                longValue = ((Number) object).longValue();
                doubleValue = null;
                booleanValue = null;
                date = null;
            }
        } else if (object instanceof Boolean) {
            text = null;
            longValue = null;
            doubleValue = null;
            booleanValue = (Boolean) object;
            date = null;
        } else if (object instanceof Date) {
            text = null;
            longValue = null;
            doubleValue = null;
            booleanValue = null;
            date = (Date) object;
        } else if (object instanceof Calendar) {
            text = null;
            longValue = null;
            doubleValue = null;
            booleanValue = null;
            date = ((Calendar) object).getTime();
        } else if (object instanceof Instant) {
            text = null;
            longValue = null;
            doubleValue = null;
            booleanValue = null;
            date = Date.from((Instant)object);
        } else {
            throw new IllegalArgumentException("Unsupported type "
                    + (object!=null?object.getClass():"null")
                    + ", must be one of "
                    + Arrays.<Class<?>> asList(String.class, Number.class, Boolean.class, Boolean.class,
                            Date.class, Calendar.class, Instant.class));
        }
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
        EmbeddableObject other = (EmbeddableObject) obj;
        return    valueOrder == other.valueOrder
                && Objects.equals(entityType, other.entityType)
                && Objects.equals(fieldName, other.fieldName)
                
                && Objects.equals(booleanValue, other.booleanValue)
                && Objects.equals(date, other.date)
                && Objects.equals(doubleValue, other.doubleValue)
                && Objects.equals(longValue, other.longValue)
                && Objects.equals(text, other.text);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(entityType, fieldName, booleanValue, date, doubleValue, longValue, text);
        hash = 31 * hash + valueOrder;
        return hash;
    }

    @Override
    public String toString() {
        Object obj = getObject();
        return getFieldName()+"="+obj.getClass().getSimpleName() + ":" + obj;
    }

    public static boolean isSupportWrap(Object object) {
        return object instanceof String || object instanceof Number
                || object instanceof Boolean || object instanceof Date || object instanceof Calendar 
                || object instanceof Instant;
    }
}
