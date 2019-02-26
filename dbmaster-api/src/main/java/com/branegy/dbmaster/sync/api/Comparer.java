package com.branegy.dbmaster.sync.api;

/**
 * A Comparer is responsible to matching source and target objects and interfaces helps to compare hierarchy of objects
 */
public interface Comparer {

    /**
     * Compares source and target object attributes, sets compare results and defines child pairs in the hierarchy
     * @param pair      a sync pair that keeps source and target objects and all context necessary for compare and later synchronize objects
     * @param session   a instance of current sync session
     */
    void syncPair(SyncPair pair, SyncSession session);
    
    /**
     * Matches two objects and returns similarity of the objects. Used to detect duplicated and renamed objects.
     * Source and target are on the same hierarchy level and objects of the same type
     * @param source   a sync pair with object that can possibly be copied or renamed
     * @param target   a sync pair with status <code>NEW</code>
     * @param session  a instance of current sync session
     * @return similarity rating (a float between 0 and 1 inclusive)
     */
    // TODO Rename source and target for better terminology
    float matchObjects(SyncPair source, SyncPair target, SyncSession session);
}