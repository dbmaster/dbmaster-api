package com.branegy.dbmaster.sync.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Structure to detect renames
 */
public class MatchPair {
    private static final AtomicLong GLOBAL_ID = new AtomicLong();
    
    final long id;
    boolean selected;
    String sourceName;
    String targetName;
    SyncPair source;
    SyncPair target;
    float similarity;
    String objectType;
   
    List<SyncAttributePair> attributes;
    
    long sourcePairId;

    public MatchPair(SyncPair sourcePair, SyncPair targetPair, float similarity) {
        this.id = GLOBAL_ID.getAndIncrement();
        selected = false;
        sourceName = sourcePair.getUniqueSourceName();
        source = sourcePair;
        targetName = targetPair.getUniqueTargetName();
        target = targetPair;
        this.similarity = similarity;
        objectType = sourcePair.getObjectType();
    }
    
    public long getId() {
        return id;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<SyncAttributePair> getAttributes() {
        if (attributes == null){
            attributes = new ArrayList<SyncAttributePair>();
        }
        return attributes;
    }

    public SyncPair getTarget() {
        return target;
    }

    public float getSimilarity() {
        return similarity;
    }

    public SyncPair getSource() {
        return source;
    }
    

}
