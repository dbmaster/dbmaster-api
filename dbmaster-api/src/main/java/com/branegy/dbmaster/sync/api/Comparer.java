package com.branegy.dbmaster.sync.api;

/**
 * Comparer helps to compare hierarchy of objects
 */
public interface Comparer {
    void syncPair(SyncPair pair, SyncSession session);
    
    /**
     * Match two objects and returns similarity of the objects. Used to find duplicates and renames.
     * 
     * @return similarity rating (a float between 0 and 1 inclusive)
     */
    float matchObjects(SyncPair source, SyncPair target, SyncSession session);
}