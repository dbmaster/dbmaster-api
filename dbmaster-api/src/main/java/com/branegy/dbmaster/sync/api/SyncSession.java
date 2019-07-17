package com.branegy.dbmaster.sync.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import com.branegy.dbmaster.sync.api.SyncPair.ChangeType;
import com.branegy.dbmaster.util.NameMap;
import com.branegy.dbmaster.util.StringLogger;

public abstract class SyncSession {
    
    Map<String, Object> parameters;
    
    private SyncPair syncResult;
    
    private Comparer comparer;
    
    private Namer namer;
    
    protected Logger logger;
    
    protected final Date lastSyncDate = new Date();
    
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
     * This method identifies similar objects for selected child items.
     * @param contextPair          a scope for search
     * @param searchWithinDeleted  identifies if search will attempt to search within previously deleted objects
     * @param minSimilarity        a float between 0 and 1 to filter out non-matching pairs
     * @return a list of pairs that match level is above <code>minSimilarity</code>
     */
    public synchronized List<MatchPair> findSimilarSourceObjects(SyncPair contextPair,
            boolean searchWithinDeleted, float minSimilarity) {
        List<MatchPair> similarityResults = new ArrayList<MatchPair>();

        for (SyncPair targetPair : contextPair.getChildren()) {
            if (targetPair.isSelected()) {
                searchMatches(similarityResults, getSyncResult(), targetPair, minSimilarity, searchWithinDeleted);
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

    private void searchMatches(List<MatchPair> similarityResults, SyncPair sourcePair, SyncPair targetPair, 
            float minSimilarity, boolean searchWithinDeleted) {
        if (sourcePair.getId() == targetPair.getId()) {
            return;
        }
        for (SyncPair child : sourcePair.getChildren()) {
            searchMatches(similarityResults,child, targetPair, minSimilarity, searchWithinDeleted);
        }
        if (searchWithinDeleted) {
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
    
    public synchronized List<SyncPair> findObjects(String query, SyncPair typePattern, SearchTarget target) {
        SearchBin searchBin = new SearchBin();
        searchBin.query = query==null?null:query.toUpperCase();
        searchBin.objectType = typePattern == null? null: typePattern.getObjectType(); // TODO fix impl
        searchBin.target = target;
        findObject(searchBin,getSyncResult(), "");
        return searchBin.results;
    }
    
    
    private static class AutoRenameBin{
        List<SyncPair> deletedSyncPair = new ArrayList<>();
        List<SyncPair> newSyncPair = new ArrayList<>();
    }
    
    
    private void traversalRename(List<AutoRenameBin> result, SyncPair pair, String objectType) {
        AutoRenameBin current = null;
        for (SyncPair child:pair.getChildren()) {
            if (objectType.equals(child.getObjectType())) { // TODO filter ?
                if (current == null) {
                    current = new AutoRenameBin();
                }
                if (child.getChangeType() == ChangeType.NEW) {
                    current.newSyncPair.add(child);
                } else if (child.getChangeType() == ChangeType.DELETED) {
                    current.deletedSyncPair.add(child);
                }
            } else if (current==null) {
                traversalRename(result,child,objectType); 
            }
        }
        if (current!=null) {
            result.add(current);
        }
    }
    
    private boolean different(List<SyncPair> pairs) {
        for (int i=0, len = pairs.size(); i<len; ++i) {
            for (int j=i+1; j<len;++j) {
                if (deepEqualsSyncPair(pairs.get(i), pairs.get(j))) {
                    logger.warn("Can't process mapping: {} is similar to {}",pairs.get(i).getSourceName(),
                            pairs.get(j).getTargetName());
                    return false;
                }
            }
        }
        return true;
    }
    
    private void onlyDifferent(List<SyncPair> pairs) {
        for (int i=0; i<pairs.size(); ++i) {
            for (int j=i+1; j<pairs.size();++j) {
                if (deepEqualsSyncPair(pairs.get(i), pairs.get(j))) {
                    pairs.remove(j);
                    pairs.remove(i);
                    --i;
                    break;
                }
            }
        }
    }
    
    private static <T> NameMap<T> toMap(List<T> list,Function<T, String> getName){
        NameMap<T> map = list.stream().collect(
                Collectors.toMap(
                    getName,
                    Function.identity(),
                    (u,v) -> { 
                        throw new IllegalStateException(String.format("Duplicate key %s", u)); 
                    },
                    NameMap<T>::new)
            );
        return map;
    }
    
    private static final boolean deepEqualsSyncPair(SyncPair source, SyncPair target) {
        /*if (compareName) {
            boolean result;
            if (source.isCaseSensitive() || target.isCaseSensitive() || 
                   (source.getParentPair()!=null && source.getParentPair().isCaseSensitive()) ||
                   (target.getParentPair()!=null && target.getParentPair().isCaseSensitive())) {
                result = source.getSourceName().equals(target.getTargetName());
            } else {
                result = source.getSourceName().equalsIgnoreCase(target.getTargetName());
            }
            if (!result) {
                return false;
            }
        }*/
        
        NameMap<SyncAttributePair> sourceAttrs = toMap(source.getAttributes(),
                SyncAttributePair::getAttributeName);
        NameMap<SyncAttributePair> targetAttrs = toMap(target.getAttributes(),
                SyncAttributePair::getAttributeName);
        
        if (sourceAttrs.size()!=targetAttrs.size()) {
            return false;
        }
        if (sourceAttrs.entrySet().stream().filter( e->{ 
            String key = e.getKey();
            Object v1 = e.getValue().getSourceValue();
            Object v2 = targetAttrs.get(key).getTargetValue();
            if (v2 == null && !targetAttrs.containsKey(key)) {
                return true;
            }
            return !Objects.equals(v1, v2);
        }).findFirst().isPresent()) {
            return false;
        }
        
        Function<SyncPair, String> getName = p -> {
            if (p.getChangeType() == ChangeType.NEW) {
                return p.getTargetName();
            } else if (p.getChangeType() == ChangeType.DELETED) {
                return p.getSourceName();
            } else {
                throw new IllegalArgumentException("Unknown sync type");
            }
        };
        NameMap<SyncPair> sourcePairs = toMap(source.getChildren(), getName);
        NameMap<SyncPair> targetPairs = toMap(target.getChildren(), getName);
        
        if (sourcePairs.size()!=targetPairs.size()) {
            return false;
        }
        
        if (sourcePairs.entrySet().stream().filter(e->{ 
            String key = e.getKey();
            SyncPair childSource = e.getValue();
            SyncPair childTarget = targetPairs.get(key);
            if (childTarget == null) { //  && !targetPairs.containsKey(key)
                return true;
            }
            return !deepEqualsSyncPair(childSource,childTarget);
        }).findFirst().isPresent()) {
            return false;
        }
        return true;
    }
    
    
    // one parent
    // different name
    public final List<SyncPair> autoRename(String objectType, String filter){
        List<AutoRenameBin> renameBins = new ArrayList<>();
        traversalRename(renameBins, getSyncResult(), objectType);
        
        List<SyncPair> result = new ArrayList<>();
        for (AutoRenameBin bin:renameBins) {
            
            if (!different(bin.deletedSyncPair)) {
                continue;
            }
            if (!different(bin.newSyncPair)) {
                continue;
            }
            
            Iterator<SyncPair> deletedIt = bin.deletedSyncPair.iterator();
            while (deletedIt.hasNext()){
                SyncPair deletedPair = deletedIt.next();
                Iterator<SyncPair> newIt = bin.newSyncPair.iterator();
                while (newIt.hasNext()) {
                    SyncPair newPair = newIt.next();
                    if (deepEqualsSyncPair(deletedPair, newPair)) {
                        deletedIt.remove();
                        newIt.remove();
                        
                        SyncPair parent = deletedPair.getParentPair();
                        SyncPair pair = new SyncPair(parent, deletedPair.getSource(), newPair.getTarget());
                        
                        Iterator<SyncPair> it = parent.getChildren().iterator();
                        int count = 0;
                        while (it.hasNext()) {
                            SyncPair p = it.next();
                            if (p == deletedPair || p == newPair) {
                                it.remove();
                                if (++count==2) {
                                    break;
                                }
                            }
                        }
                        parent.getChildren().add(pair);
                        pair.sync(this);
                        result.add(pair);
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private static class AutoMoveBin{
        Map<String,List<SyncPair>> deletedSyncPair = new LinkedHashMap<>();
        Map<String,List<SyncPair>> newSyncPair = new LinkedHashMap<>();
    }
    
    private void traversalMove(AutoMoveBin current, SyncPair pair, String objectType) {
        for (SyncPair child:pair.getChildren()) {
            if (objectType.equals(child.getObjectType())) { // TODO filter ?
                if (child.getChangeType() == ChangeType.NEW) {
                    current.newSyncPair.computeIfAbsent(child.getTargetName(),(k)-> new ArrayList<>()).add(child);
                } else if (child.getChangeType() == ChangeType.DELETED) {
                    current.deletedSyncPair.computeIfAbsent(child.getSourceName(),(k)-> new ArrayList<>()).add(child);
                }
            } else {
                traversalMove(current,child,objectType); 
            }
        }
    }
    
    // different parent
    // some name
    public final List<SyncPair> autoMove(String objectType, String filter){
        AutoMoveBin bin = new AutoMoveBin();
        traversalMove(bin, getSyncResult(), objectType);
        
        List<SyncPair> result = new ArrayList<>();
        for (Entry<String,List<SyncPair>> e:bin.deletedSyncPair.entrySet()) {
            String key = e.getKey();
            List<SyncPair> deletedPairs = e.getValue();
            List<SyncPair> newPairs = bin.newSyncPair.get(key);
            if (newPairs == null) {
                continue;
            }
            
            onlyDifferent(deletedPairs);
            onlyDifferent(newPairs);
            
            for (int i=0; i<deletedPairs.size(); ++i) {
                for (int j=0; j<newPairs.size(); ++j) {
                    if (deepEqualsSyncPair(deletedPairs.get(i), newPairs.get(j))) {
                        // add move!
                        SyncPair deletePair = deletedPairs.get(i);
                        SyncPair newPair = newPairs.get(j);
                        
                        Iterator<SyncPair> it;
                        it = deletePair.getParentPair().getChildren().iterator();
                        while (it.hasNext()) {
                            if (it.next() == deletePair) {
                                it.remove();
                                break;
                            }
                        }
                        it = newPair.getParentPair().getChildren().iterator();
                        while (it.hasNext()) {
                            if (it.next() == newPair) {
                                it.remove();
                                break;
                            }
                        }
                        SyncPair pair = new SyncPair(newPair.getParentPair(), 
                                deletePair.getSource(),newPair.getTarget());
                        newPair.getParentPair().getChildren().add(pair);
                        pair.sync(this);
                        result.add(pair);
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private void findObject(SearchBin searchBin, SyncPair pair, String path) {
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
            findObject(searchBin,child, newPath);
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
        if ("error".equalsIgnoreCase(level)) {
            logger.error(message);
        }
    }
    
    public Logger getLogger() {
        return logger;
    }

    @SuppressWarnings("unchecked")
    protected final <T> void applyChangesForLists(List<T> source, List<T> target, SyncPair parentPair, String type) {
        Preconditions.checkNotNull(source,      "Parameter source can not be null");
        Preconditions.checkNotNull(target,      "Parameter target can not be null");
        Preconditions.checkNotNull(type,        "Parameter type can not be null");
        Preconditions.checkNotNull(parentPair,  "Parameter parentPair can not be null");
        
        List<SyncPair> children = subListByType(parentPair, type);
        int addIndexShift = 0;
        for (SyncPair p:children) {
            Preconditions.checkArgument(p.isOrdered(),"SyncPair should be ordered for "+p.toString());
            if (!p.isSelected()) {
                if (p.getChangeType() == ChangeType.NEW) {
                    addIndexShift++;
                }
                continue;
            }
            T sourceObj = (T) p.getSource();
            T targetObj = (T) p.getTarget();
            switch (p.getChangeType()) {
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