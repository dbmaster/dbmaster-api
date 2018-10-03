package com.branegy.dbmaster.sync.api;

import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.CHANGED;
import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.DELETED;
import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.EQUALS;
import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.NEW;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

// Not thread-safe!
public class SyncAttributePair {
    private static final AtomicLong GLOBAL_ID = new AtomicLong();
    static final SyncAttributeComparator<Object> DEFAULT_COMPARATOR = new SyncAttributeComparator<Object>() {
        @Override
        public SyncAttributePair.AttributeChangeType compare(Object sourceValue, Object targetValue) {
            if (sourceValue == null) {
                if (targetValue==null) {
                    return EQUALS;
                } else {
                    return NEW;
                }
            } else if (targetValue == null) {
                return DELETED;
            } else {
                boolean equals;
                if (sourceValue instanceof Number && targetValue instanceof Number){
                    equals = ((Number)sourceValue).doubleValue() == ((Number)targetValue).doubleValue();
                } else  if (sourceValue instanceof Date && targetValue instanceof Date) {
                    equals = ((Date)sourceValue).compareTo((Date)targetValue) == 0;
                } else {
                    equals = sourceValue.equals(targetValue);
                }
                return equals ? EQUALS : CHANGED;
            }
        }
    };
    
    public static enum AttributeChangeType {
        NEW, CHANGED, DELETED, EQUALS
    }
    
    public interface SyncAttributeComparator<T>{
        AttributeChangeType compare(T sourceValue,T targetValue);
    }

    private String attributeName;
    private Object sourceValue;
    private Object targetValue;
    private boolean selected;
    private boolean ignorable;
    private AttributeChangeType changeType;
    private final long id;

    public <T> SyncAttributePair(String attributeName, T sourceValue, T targetValue) {
        this(attributeName, sourceValue, targetValue, DEFAULT_COMPARATOR);
    }
    
    public <T> SyncAttributePair(String attributeName, T sourceValue, T targetValue,
            SyncAttributeComparator<T> comparator) {
        this.attributeName = attributeName;
        this.sourceValue = sourceValue;
        this.targetValue = targetValue;
        this.selected = true;
        this.changeType = comparator.compare(sourceValue, targetValue);
        this.id = GLOBAL_ID.getAndIncrement();
    }
    
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Object getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(Object sourceValue) {
        this.sourceValue = sourceValue;
    }

    public Object getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Object targetValue) {
        this.targetValue = targetValue;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public AttributeChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(AttributeChangeType changeType) {
        this.changeType = changeType;
    }

    @Override
    public String toString() {
        return attributeName + "(" + changeType + ")" + sourceValue + "/" + targetValue;
    }

    public long getId() {
        return id;
    }

    public boolean isIgnorable() {
        return ignorable;
    }

    public void setIgnorable(boolean ignorable) {
        this.ignorable = ignorable;
    }

}
