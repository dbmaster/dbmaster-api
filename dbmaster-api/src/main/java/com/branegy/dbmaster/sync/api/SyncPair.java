package com.branegy.dbmaster.sync.api;

import static com.branegy.dbmaster.sync.api.SyncPair.ChangeType.CHANGED;
import static com.branegy.dbmaster.sync.api.SyncPair.ChangeType.DELETED;
import static com.branegy.dbmaster.sync.api.SyncPair.ChangeType.EQUALS;
import static com.branegy.dbmaster.sync.api.SyncPair.ChangeType.NEW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType;

// Not thread-safe!
public class SyncPair {
    private static final AtomicLong GLOBAL_ID = new AtomicLong();
    
    public static enum ChangeType {
        /** object is new. target only  */
        NEW,
        
        /** object has any change: childrenChanges || attributeChanges || isOrderChanged() || name change*/
        CHANGED,
        
        /** object is copied. source and target*/
        COPIED,
        
        
        /** object is deleted. source only*/
        DELETED,
        /** object is equals. source and target*/
        EQUALS
    }

    private ChangeType changeType;
    private String objectType;
    private String sourceName;
    private String targetName;
    private boolean caseSensitive;

    /**
     * position in source list
     */
    private Integer sourceIndex;

    /**
     * position in target list
     */
    private Integer targetIndex;
    
    private final SyncPair parentPair;

    private List<SyncPair> childPairs;
    private List<SyncAttributePair> attributes;
    
    // transient fields
    private final long id;

    private boolean selected;

    private boolean attributeChanges;

    private boolean childrenChanges;

    /**
     * ignore change for current SyncPair
     */
    private boolean ignorable;

    /**
     * ignore order change for current SyncPair
     */
    private boolean ignorableOrderChange;

    /**
     * ignore name change
     */
    private boolean ignorableNameChange;
    
    /**
     * aggregate children changes
     */
    // transient fields
    private boolean ignorableOrderChanges;
    private boolean ignorableAttributesChanges;
    private boolean ignorableChildrenChanges;
    
    private Object source;
    private Object target;
    private Object originalSource;
    private Object originalTarget;
    
    public SyncPair(SyncPair parentPair, Object source, Object target) {
        if (source==null && target==null) {
            throw new IllegalArgumentException("Both parameters can not be null for " + parentPair);
        }
        this.source = source;
        this.target = target;
        this.originalSource = source;
        this.originalTarget = target;
        this.parentPair = parentPair;
        this.id = GLOBAL_ID.getAndIncrement();
        this.selected = true;
    }
    
    public SyncPair(SyncPair parentPair) {
        this.source = null;
        this.target = null;
        this.originalSource = source;
        this.originalTarget = target;
        this.parentPair = parentPair;
        this.id = GLOBAL_ID.getAndIncrement();
        this.selected = true;
    }
    
    public List<SyncAttributePair> getAttributes() {
        if (attributes==null) {
            attributes = new ArrayList<SyncAttributePair>();
        }
        return attributes;
    }

    public void setAttributes(List<SyncAttributePair> attributes) {
        this.attributes = attributes;
    }

    public final List<SyncPair> getChildren() {
        if (childPairs==null) {
            childPairs = new ArrayList<SyncPair>();
        }
        return childPairs;
    }
    
    public final Object getSource() {
        return source;
    }

    public final Object getTarget() {
        return target;
    }
    
    public ChangeType getChangeType() {
        return changeType;
    }
    
    /**
     * @return true if reordered<br>
     *         false otherwise (include insert with index)
     */
    public boolean isOrderChanged(){
        return sourceIndex!=null && targetIndex!=null && !sourceIndex.equals(targetIndex);
    }
    
    public boolean isOrdered(){
        return sourceIndex!=null || targetIndex!=null;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        String toString = getChangeType()+" " + getObjectType()+" ";
        try{
            toString += getPairName();
        } catch (Exception e){
            toString += "unknown type ";
        }
        toString += "(id=" + getId();
        if (isOrdered()){
            toString += ",index="+sourceIndex+"/"+ targetIndex;
        }
        toString += ")";
        return toString;
    }

    public final String toStringTree() {
        StringBuilder builder = new StringBuilder(64*1024);
        toStringTree(builder,0,this);
        return builder.toString();
    }

    private static final void toStringTree(StringBuilder builder, int deep, SyncPair pair) {
        for (int i=0; i<deep; ++i) {
            builder.append("   ");
        }
        builder.append(pair.getChangeType());
        builder.append(' ');
        builder.append(pair.getObjectType());
        try{
            builder.append(pair.getPairName());
            builder.append(' ');
        } catch (Exception e){
            builder.append("unknown type ");
        }
        if (pair.isOrdered()){
            builder.append('[');
            builder.append(pair.sourceIndex);
            builder.append('/');
            builder.append(pair.targetIndex);
            builder.append("] ");
        }
        for (SyncAttributePair attr:pair.getAttributes()) {
            builder.append('\n');
            for (int i=0; i<=deep; ++i) {
                builder.append("   ");
            }
            builder.append(attr.getChangeType());
            builder.append(' ');
            builder.append(attr.getAttributeName());
        }
        
        if (pair.attributeChanges) {
            builder.append(" *attr");
        }
        if (pair.childrenChanges) {
            builder.append(" *children");
        }
        
        if (pair.isChildren()) {
            for (SyncPair child:pair.getChildren()) {
                toStringTree(builder, deep+1, child);
            }
        }
    }

    public String getPairName() {
        String toString = null;
        switch (getChangeType()) {
        case NEW:
            toString = getTargetName();
            break;
        case CHANGED:
            toString = getTargetName();
            break;
        case COPIED:
            toString = getTargetName()+"(renamed from " + getSourceName()+")";
            break;
        case DELETED:
            toString = getSourceName();
            break;
        case EQUALS:
            toString = getSourceName();
            break;
        default:
            throw new RuntimeException("Unexpected change type ("+getChangeType()+")");
        }
        return toString;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * Cleans sync attributes and child items;
     */
    public void clear() {
        if (childPairs!=null) childPairs.clear();
        if (attributes!=null) attributes.clear();
        targetName = null;
        sourceName = null;
        changeType = null;
        childrenChanges = false;
        attributeChanges = false;
        ignorableChildrenChanges = false;
        ignorableAttributesChanges = false;
        ignorableOrderChanges = false;
    }
    
    public String getUniqueSourceName() {
        String parentPrefix = parentPair == null ? "" : parentPair.getUniqueSourceName()+".";
        if (source==null) {
            return null;
        } else {
            return parentPrefix + sourceName;
        }
    }
    
    public String getUniqueTargetName() {
        String parentPrefix = parentPair == null ? "" : parentPair.getUniqueTargetName()+".";
        if (target==null) {
            return null;
        } else {
            return parentPrefix + targetName;
        }
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    
    public void setSource(Object source) {
        this.source = source;
    }
    
    public void rollbackChanges(SyncSession session) {
        source = originalSource;
        target = originalTarget;
        sync(session);
    }
    
    public final void sync(SyncSession session) {
        clear();
        Namer namer = session.getNamer();
        if (source==null) {
            changeType = NEW;
            sourceName = null;
            targetName = namer.getName(target);
            objectType = namer.getType(target);
            attributeChanges = attributes!=null && !attributes.isEmpty();
        } else if (target==null) {
            changeType = DELETED;
            sourceName = namer.getName(source);
            targetName = null;
            objectType = namer.getType(source);
            attributeChanges = attributes!=null && !attributes.isEmpty();
        } else {
            objectType = namer.getType(source);
            targetName = namer.getName(target);
            sourceName = namer.getName(source);
            //changeType = !ignorableNameChange && !sourceName.equalsIgnoreCase(targetName)?CHANGED:EQUALS;
            changeType = !ignorableNameChange && !equalsString(caseSensitive, sourceName, targetName)
                    ?CHANGED
                    :EQUALS;
            
            assert namer.getType(source).equals(namer.getType(target));
        }
        Comparer cmp = session.getComparer();
        cmp.syncPair(this, session);
        
        // compare child pairs
        for (SyncPair childPair : getChildren()) {
            childPair.sync(session);
            if (childPair.getChangeType()!=EQUALS) {
                if (childPair.isIgnorable()) {
                    ignorableChildrenChanges = true;
                } else {
                    childrenChanges = true;
                }
            }
            if (childPair.isOrderChanged()) {
                if (childPair.isIgnorableOrderChange()) {
                    ignorableOrderChanges = true;
                } else {
                    childrenChanges = true;
                }
            }
            if (childPair.ignorableAttributesChanges || childPair.ignorableChildrenChanges
                    || childPair.ignorableOrderChanges ) {
                ignorableChildrenChanges = true;
            }
            if (childPair.attributeChanges) {
                childrenChanges = true;
            }
        }

        for (SyncAttributePair attribute : getAttributes()) {
            if (attribute.getChangeType() != AttributeChangeType.EQUALS) {
                if (attribute.isIgnorable()) {
                    ignorableAttributesChanges = true;
                } else {
                    attributeChanges = true;
                }
            }
        }
        if (changeType == EQUALS && (attributeChanges || childrenChanges)) { // do not check name again
            changeType = CHANGED;
        }
    }

    public String getObjectType() {
        return objectType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }
    
    public SyncPair findChildById(long id) {
        if (childPairs != null){
            for (SyncPair pair:childPairs) {
                if (pair.getId() == id) {
                    return pair;
                }
            }
        }
        return null;
    }
    
    public SyncAttributePair findAttrubuteById(long id) {
        if (attributes != null){
            for (SyncAttributePair attr:attributes){
                if (attr.getId() == id){
                    return attr;
                }
            }
        }
        return null;
    }

    public long getId() {
        return id;
    }
    
    public boolean isChildren() {
        return childPairs != null && !childPairs.isEmpty();
    }

    public SyncPair getParentPair() {
        return parentPair;
    }

    public void setChildPairs(List<SyncPair> childPairs) {
        this.childPairs = childPairs;
    }

    public Integer getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(Integer sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public Integer getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(Integer targetIndex) {
        this.targetIndex = targetIndex;
    }

    public boolean isAttributeChanges() {
        return attributeChanges;
    }

    public boolean isChildrenChanges() {
        return childrenChanges;
    }
    
    public boolean isIgnorable() {
        return ignorable;
    }

    public void setIgnorable(boolean ignorable) {
        this.ignorable = ignorable;
    }

    public boolean isIgnorableAttributesChanges() {
        return ignorableAttributesChanges;
    }

    public boolean isIgnorableChildrenChanges() {
        return ignorableChildrenChanges;
    }

    public boolean isIgnorableOrderChanges() {
        return ignorableOrderChanges;
    }

    public boolean isIgnorableOrderChange() {
        return ignorableOrderChange;
    }

    public void setIgnorableOrderChange(boolean ignorableOrderChange) {
        this.ignorableOrderChange = ignorableOrderChange;
    }
    
    public boolean isNameChange() {
        // return sourceName!=null && targetName!=null && !sourceName.equalsIgnoreCase(targetName);
        return sourceName!=null && targetName!=null && !equalsString(caseSensitive,sourceName,targetName);
    }
    
    private static boolean equalsString(boolean caseSensitive, String s1, String s2) {
        return s1.regionMatches(!caseSensitive, 0, s2, 0, s2.length());
    }
    
    public boolean isIgnorableNameChange() {
        return ignorableNameChange;
    }

    public void setIgnorableNameChange(boolean ignorableNameChange) {
        this.ignorableNameChange = ignorableNameChange;
    }

    public final boolean isCaseSensitive() {
        return caseSensitive;
    }

    public final void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * workaround for new api. do not use it!
     */
    // TODO add new api
    @Deprecated
    public void refreshFlags() {
        for (SyncPair childPair :  getChildren()) {
            if (childPair.getChangeType()!=EQUALS || childPair.isOrderChanged()){
                childrenChanges = true;
                break;
            }
        }
        for (SyncAttributePair attribute : getAttributes()) {
            if (attribute.getChangeType() != AttributeChangeType.EQUALS) {
                attributeChanges = true;
                break;
            }
        }
    }    
}