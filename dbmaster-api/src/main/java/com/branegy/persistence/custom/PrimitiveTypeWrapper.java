package com.branegy.persistence.custom;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

@Embeddable
final class PrimitiveTypeWrapper {

    @Column(length=4*1024*1024)
    @Size(min=1, max=4*1024*1024)
    private String text;
    
    private Long integer;
    
    private Double fractional;
    
    private Boolean bool;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;
    
    @Column(name="NOT_NULL_COLUMN",nullable=false)
    @Deprecated 
    // For Hibernate 4.3.7 HHH-7072 workaround
    // org.hibernate.collection.internal.AbstractPersistentCollection.needsRecreate(CollectionPersister)
    private int fakeNotNullColumn; 

    @SuppressWarnings("unused")
    private PrimitiveTypeWrapper() {
    }

    PrimitiveTypeWrapper(Object object) {
        setObject(object);
    }

    Object getObject() {
        if (text != null) {
            return text;
        } else if (integer != null) {
            return integer;
        } else if (fractional != null) {
            return fractional;
        } else if (bool != null) {
            return bool;
        } else if (time != null) {
            return time;
        } else {
            throw new IllegalArgumentException("All fields can't be null");
        }
    }

    void setObject(Object object) {
        if (object instanceof String) {
            text = (String) object;
            integer = null;
            fractional = null;
            bool = null;
            time = null;
        } else if (object instanceof Number) {
            if (object instanceof Double || object instanceof Float) {
                text = null;
                integer = null;
                fractional = ((Number) object).doubleValue();
                bool = null;
                time = null;
            } else {
                text = null;
                integer = ((Number) object).longValue();
                fractional = null;
                bool = null;
                time = null;
            }
        } else if (object instanceof Boolean) {
            text = null;
            integer = null;
            fractional = null;
            bool = (Boolean) object;
            time = null;
        } else if (object instanceof Date) {
            text = null;
            integer = null;
            fractional = null;
            bool = null;
            time = (Date) object;
        } else if (object instanceof Calendar) {
            text = null;
            integer = null;
            fractional = null;
            bool = null;
            time = ((Calendar) object).getTime();
        } else {
            throw new IllegalArgumentException("Unsupported type "
                    + (object!=null?object.getClass():"null")
                    + ", must be one of "
                    + Arrays.<Class<?>> asList(String.class, Number.class, Boolean.class, Boolean.class,
                            Date.class, Calendar.class));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PrimitiveTypeWrapper)) {
            return false;
        }
        PrimitiveTypeWrapper other = (PrimitiveTypeWrapper) obj;
        if (bool == null) {
            if (other.bool != null) {
                return false;
            }
        } else if (!bool.equals(other.bool)) {
            return false;
        }
        if (fractional == null) {
            if (other.fractional != null) {
                return false;
            }
        } else if (!fractional.equals(other.fractional)) {
            return false;
        }
        if (integer == null) {
            if (other.integer != null) {
                return false;
            }
        } else if (!integer.equals(other.integer)) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        if (time == null) {
            if (other.time != null) {
                return false;
            }
        } else if (!time.equals(other.time)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bool == null) ? 0 : bool.hashCode());
        result = prime * result + ((fractional == null) ? 0 : fractional.hashCode());
        result = prime * result + ((integer == null) ? 0 : integer.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    @Override
    public String toString() {
        Object obj = getObject();
        return obj.getClass().getSimpleName() + ":" + obj;
    }

    static boolean isSupportWrap(Object object) {
        return object instanceof String || object instanceof Number
                || object instanceof Boolean || object instanceof Date || object instanceof Calendar;
    }

}
