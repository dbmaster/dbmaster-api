package com.branegy.dbmaster.sync.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import com.branegy.dbmaster.sync.api.SyncPair.ChangeType;
import com.branegy.dbmaster.util.StringLogger;

public abstract class SyncSession {
    
    Map<String, Object> parameters;
    
    private SyncPair syncResult;
    
    private Comparer comparer;
    
    private Namer namer;
    
    protected Logger logger;
    
    public static enum SearchTarget { SOURCE, TARGET };

    public SyncSession(Comparer compararer) {
        this(compararer, null);
    }
    
    public SyncSession(Comparer compararer, Logger logger) {
        parameters = new HashMap<String,Object>();
        this.comparer = compararer;
        this.logger = logger == null ? new StringLogger() : logger;
    }

    public SyncPair syncObjects(Object source, Object target) {
        SyncPair pair = new SyncPair(null, source, target);
        this.syncResult = pair;
        pair.sync(this);
        return pair;
    }
    
    public Map<String, Object> getAllParameters() {
        return parameters;
    }
    
    public Object getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    public void setParameter(String parameterName, Object value) {
        parameters.put(parameterName, value);
    }

    public SyncPair getSyncResult() {
        return syncResult;
    }

    public Namer getNamer() {
        return namer;
    }

    public void setNamer(Namer namer) {
        this.namer = namer;
    }

    public Comparer getComparer() {
        return comparer;
    }

    public SyncPair findPairById(long id){
        return recursiveFind(syncResult, id);
    }
    
    private SyncPair recursiveFind(SyncPair pair, long id){
        if (pair.getId() != id){
            for (SyncPair child:pair.getChildren()){
                pair = recursiveFind(child, id);
                if (pair!=null){
                    return pair;
                }
            }
            pair = null;
        }
        return pair;
    }
    
    /**
     * This method should search for similar objects for selected child items.
     * @param pairId
     * @param searchWithinDeleted
     * @param minSimilarity
     * @return
     */
    public synchronized List<MatchPair> findSimilarSourceObjects(SyncPair contextPair,
            boolean searchWithinDeleted, float minSimilarity) {
        similarityResults = new ArrayList<MatchPair>();

        for (SyncPair targetPair : contextPair.getChildren()) {
            if (targetPair.isSelected()) {
                searchMatches(getSyncResult(), targetPair, minSimilarity, searchWithinDeleted);
            }
        }
        Collections.sort(similarityResults, new Comparator<MatchPair>() {
            @Override
            public int compare(MatchPair o1, MatchPair o2) {
                int result = Float.compare(o2.getSimilarity(), o1.getSimilarity());
                /*if (result == 0){
                    result = o1.getSourceName().compareToIgnoreCase(o2.getTargetName());
                }*/
                return result;
            }
        });
        return similarityResults;
    }

    /**
     * Temporary structure to hold search parameters and results.
     */
    private List<MatchPair> similarityResults;
    
    private void searchMatches(SyncPair sourcePair, SyncPair targetPair, float minSimilarity,
            boolean searchWithinDeleted) {
        if (sourcePair.getId() == targetPair.getId()) {
            return;
        }
        for (SyncPair child : sourcePair.getChildren()) {
            searchMatches(child, targetPair, minSimilarity, searchWithinDeleted);
        }
        if (searchWithinDeleted){
            if (sourcePair.getChangeType() != ChangeType.DELETED){
                return; // search within delete only
            }
        } else if (sourcePair.getChangeType() == ChangeType.NEW){
            return; // search within non new items
        } else if (!sourcePair.getObjectType().equals(targetPair.getObjectType())){
            return; // different type
        }
        float similarity = getComparer().matchObjects(sourcePair, targetPair, this);
        if (similarity >= minSimilarity) {
            MatchPair matchPair = new MatchPair(sourcePair, targetPair, similarity);
            similarityResults.add(matchPair);
        }
    }

    private static class SearchBin {
        String query;
        String objectType;
        SearchTarget target;
        List<SyncPair> results = new ArrayList<SyncPair>();
    }
    /**
     * Temporary structure to hold search parameters and results.
     */
    private SearchBin searchBin;
    
    public synchronized List<SyncPair> findObjects(String query, SyncPair typePattern, SearchTarget target) {
        searchBin = new SearchBin();
        searchBin.query = query==null?null:query.toUpperCase();
        searchBin.objectType = typePattern == null? null: typePattern.getObjectType(); // TODO fix impl
        searchBin.target = target;
        findObject(getSyncResult(), "");
        return searchBin.results;
    }
    
    private void findObject(SyncPair pair, String path) {
        String newPath;
        if (searchBin.target==SearchTarget.SOURCE) {
            if (pair.getSource() == null) {
                return;
            } else {
                newPath = pair.getSourceName().toUpperCase();
            }
        } else {
            if (pair.getTarget() == null) {
                return;
            } else {
                newPath = pair.getTargetName().toUpperCase();
            }
        }
        if (path.length()>0) {
            newPath = path + "." + newPath;
        }
        boolean termFound = newPath.contains(searchBin.query);
        if (searchBin.objectType!=null) {
            if (pair.getObjectType().equals(searchBin.objectType) && termFound) {
                searchBin.results.add(pair);
            }
        } else if (termFound) {
            searchBin.results.add(pair);
        }
        for (SyncPair child : pair.getChildren()) {
            findObject(child, newPath);
        }
    }

    /**
     * This method called when user completes review
     * and wants to merge changes.
     */
    public abstract void applyChanges();

    public void setSyncResult(SyncPair syncResult) {
        this.syncResult = syncResult;
    }
    
    @Deprecated
    /**
     * use getLogger().error() or logger.error()
     * @param level
     * @param message
     */
    public synchronized void logMessage(String level, String message) {
        if ("error".equalsIgnoreCase(level)){
            logger.error(message);
        }
    }
    
    public Logger getLogger() {
        return logger;
    }

    /**
     * merge source into target in place
     * 
     * @param source
     * @param target
     * @param pairs
     */
    @SuppressWarnings("unchecked")
    protected final <T> void applyChangesForLists(List<T> source, List<T> target,
            SyncPair parentPair, String type){
        Preconditions.checkNotNull(source,      "Parameter source can not be null");
        Preconditions.checkNotNull(target,      "Parameter target can not be null");
        Preconditions.checkNotNull(type,        "Parameter type can not be null");
        Preconditions.checkNotNull(parentPair,  "Parameter parentPair can not be null");
        
        List<SyncPair> children = subListByType(parentPair, type);
        int addIndexShift = 0;
        for (SyncPair p:children){
            Preconditions.checkArgument(p.isOrdered(),"SyncPair should be ordered for "+p.toString());
            if (!p.isSelected()){
                if (p.getChangeType() == ChangeType.NEW){
                    addIndexShift++;
                }
                continue;
            }
            T sourceObj = (T) p.getSource();
            T targetObj = (T) p.getTarget();
            switch (p.getChangeType()){
            case NEW:
                if (indexOfByObject(source, namer, targetObj)!=-1){
                    throw new IllegalStateException("Object already exists "
                            +namer.getType(targetObj)+":"+namer.getName(targetObj)+" in source");
                }
                source.add(Math.min(source.size(), p.getTargetIndex()-addIndexShift), targetObj); // new
                break;
            case COPIED:
            case CHANGED:
            case EQUALS:
                int sourceIndex = indexOfByName(source, namer,
                        p.getTargetName(), namer.getType(targetObj));
                if (sourceIndex == -1){
                    throw new IllegalStateException("Object absents "
                            +namer.getType(targetObj)+":"+p.getTargetName()+" in source");
                }
                if (target.get(p.getTargetIndex()) != targetObj){
                    throw new IllegalStateException("Broken target list");
                }
                int targetIndex = Math.min(source.size(), p.getTargetIndex()-addIndexShift);
                if (sourceIndex!=targetIndex){
                    // sourceIndex always below then target -> safe to remove without lost an indexation
                    T object = source.remove(sourceIndex);
                    source.add(targetIndex, object);
                }
                break;
            case DELETED:
                int currentIndex = indexOfByObject(source, namer, sourceObj);
                if (currentIndex == -1){
                    throw new IllegalStateException("Object absents "
                            +namer.getType(sourceObj)+":"+namer.getName(sourceObj)+" in source");
                }
                source.remove(currentIndex);
                break;
            }
        }
    }

    private List<SyncPair> subListByType(SyncPair parentPair, String type) {
        List<SyncPair> children = parentPair.getChildren();
        preconditionSort(children);
        int i=0;
        for (; i<children.size(); ++i){ // TODO (Vitali) replace with binary search
            if (type.equals(children.get(i).getObjectType())){
                break;
            }
        }
        if (i==children.size()){
            return Collections.emptyList();
        }
        int j=i+1;
        for (; j<children.size(); ++j){ // TODO (Vitali) replace with binary search
            if (!type.equals(children.get(j).getObjectType())){
                break;
            }
        }
        children = children.subList(i, j);
        return children;
    }
    
    private int indexOfByObject(List<?> list, Namer namer, Object object){
        return indexOfByName(list, namer, namer.getName(object), namer.getType(object));
    }
    
    private int indexOfByName(List<?> list, Namer namer, String name, String type){
        for (int i=0; i<list.size(); ++i){
            if (name.equalsIgnoreCase(namer.getName(list.get(i))) && type.equals(namer.getType(list.get(i)))){
                return i;
            }
        }
        return -1;
    }
    
    private void preconditionSort(List<SyncPair> list){
        // sort by type asc, target index asc (nulls last)
        Collections.sort(list, new Comparator<SyncPair>() {
            private int getSyncPairAsType(SyncPair pair){
                if (pair.isOrdered()){
                    return 0;
                } else {
                   return 1;
                }
            }
            @Override
            public int compare(SyncPair o1, SyncPair o2) {
                int result = o1.getObjectType().compareTo(o2.getObjectType());
                if (result == 0){
                    int type1 = getSyncPairAsType(o1);
                    int type2 = getSyncPairAsType(o2);
                    result = type1-type2;
                    if (result == 0 && type1 == 0){
                        result = compareTo(o1.getTargetIndex(),o2.getTargetIndex());
                    }
                }
                return result;
            }
            private <T extends Comparable<T>> int compareTo(T o1, T o2){
                if (o1 == null && o2 == null){
                    return  0;
                } else if (o1 == null){
                    return +1;
                } else if (o2 == null){
                    return -1;
                } else {
                    return o1.compareTo(o2);
                }
            }
        });
    }
    
}