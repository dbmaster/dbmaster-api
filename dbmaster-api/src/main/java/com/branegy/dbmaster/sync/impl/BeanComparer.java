package com.branegy.dbmaster.sync.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.branegy.dbmaster.sync.api.Comparer;
import com.branegy.dbmaster.sync.api.Namer;
import com.branegy.dbmaster.sync.api.SyncAttributePair;
import com.branegy.dbmaster.sync.api.SyncException;
import com.branegy.dbmaster.sync.api.SyncPair;
import com.branegy.dbmaster.sync.api.SyncSession;
import com.branegy.dbmaster.util.NameMap;
import com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType;


public abstract class BeanComparer implements Comparer {

    /**
     * Merges two collections of any objects by name provided by <code>namer</code>.
     */
    public List<SyncPair> mergeCollections(
            SyncPair parentPair, Collection<?> sourceC, Collection<?> targetC, Namer namer) {
        List<SyncPair> mergedList;
        if (sourceC == null && targetC == null) {
            mergedList = new ArrayList<SyncPair>(0);
        } else if (targetC == null) {
            mergedList = new ArrayList<SyncPair>(sourceC.size());
            for (Object sourceO : sourceC) {
                mergedList.add(new SyncPair(parentPair, sourceO, null));
            }
        } else if (sourceC == null) {
            mergedList = new ArrayList<SyncPair>(targetC.size());
            for (Object targetO : targetC) {
                mergedList.add(new SyncPair(parentPair, null, targetO));
            }
        } else {
            int targetSize = targetC.size();
            Map<String, Object> targetMap = newNameMap(parentPair.isCaseSensitive(),targetSize);
            for (Object targetO : targetC) {
                String name = namer.getName(targetO);
                if (targetMap.put(name, targetO) != null) {
                    String type = namer.getType(targetO);
                    throw new SyncException("Collection already has " + type + " named " + name);
                }
            }
            mergedList = new ArrayList<SyncPair>(Math.max(sourceC.size(), targetSize));
            for (Object sourceO : sourceC) {
                String name = namer.getName(sourceO);
                Object targetO = targetMap.remove(name);
                mergedList.add(new SyncPair(parentPair, sourceO, targetO));
            }
            for (Object targetO : targetMap.values()) {
                mergedList.add(new SyncPair(parentPair, null, targetO));
            }
        }
        return mergedList;
    }
    
    private static <V> Map<String,V> newNameMap(boolean caseSensitive, int initSize){
        return caseSensitive?new LinkedHashMap<String, V>(initSize):new NameMap<V>(initSize);
    }
    
    /**
     * Interface for key of any object. Also can resolve multiple collisions grouped by some key, returned by
     * getKey(T o)
     * 
     * @param <T>
     */
    public interface KeySupplier<T>{
        String getName(T o);
        String getKey(T o);
        List<SyncPair> resolveCollisions(SyncPair parentPair, String key,
                List<T> sourceObjects, List<T> targetObjects);
    }
    
    /**
     * Resolve collision by merge source and target objects into pair step by step
     * @param <T>
     */
    public static abstract class MergeKeySupplier<T> implements KeySupplier<T>{

        @Override
        public List<SyncPair> resolveCollisions(SyncPair parentPair, String key, List<T> sourceObjects,
                List<T> targetObjects) {
            List<SyncPair> result = new ArrayList<>(Math.max(sourceObjects.size(),targetObjects.size()));
            int commonLen = Math.min(sourceObjects.size(), targetObjects.size());
            for (int i=0; i<commonLen; ++i) {
                result.add(new SyncPair(parentPair,sourceObjects.get(i),targetObjects.get(i)));
            }
            for (int i=commonLen; i<targetObjects.size(); ++i) {
                result.add(new SyncPair(parentPair,null,targetObjects.get(i)));
            }
            for (int i=commonLen; i<sourceObjects.size(); ++i) {
                result.add(new SyncPair(parentPair,sourceObjects.get(i),null));
            }
            return result;
        }
        
    }
    
    private static <T> Map<String,List<T>> groupByKey(boolean caseSensitive, Collection<T> collection,
            KeySupplier<T> keySupplier){
        Map<String,List<T>> map = newNameMap(caseSensitive, collection.size());
        for (T obj : collection) {
            String key = keySupplier.getKey(obj);
            List<T> list = map.get(key);
            if (list == null) {
                map.put(key, list = Collections.singletonList(obj));
            } else if (list.size() == 1) {
                List<T> copy = new ArrayList<>(4);
                copy.add(list.get(0));
                copy.add(obj);
                map.put(key,list = copy);
            } else {
                list.add(obj);
            }
        }
        return map;
    }
    
    private static <T> Map<String,T> byName(boolean caseSensitive, Collection<T> collection,
            KeySupplier<T> keySupplier){
        Map<String,T> map = newNameMap(caseSensitive, collection.size());
        for (T obj : collection) {
            String name = keySupplier.getName(obj);
            if (map.put(name, obj)!=null) {
                throw new SyncException("Collection already has named " + name);
            }
        }
        return map;
    }
    
    /**
     * Merges two collections of any objects by name and by key provided by <code>keySupplier</code>.
     */
    public <T> List<SyncPair> mergeCollections(
            SyncPair parentPair, Collection<T> sourceC, Collection<T> targetC, KeySupplier<T> keySupplier) {
        List<SyncPair> mergedList;
        boolean emptySource = sourceC == null || sourceC.isEmpty();
        boolean emptyTarget = targetC == null || targetC.isEmpty();
        if (emptySource && emptyTarget) {
            mergedList = new ArrayList<SyncPair>(0);
        } else if (emptyTarget) {
            mergedList = new ArrayList<SyncPair>(sourceC.size());
            for (Object sourceO : sourceC) {
                mergedList.add(new SyncPair(parentPair, sourceO, null));
            }
        } else if (emptySource) {
            mergedList = new ArrayList<SyncPair>(targetC.size());
            for (Object targetO : targetC) {
                mergedList.add(new SyncPair(parentPair, null, targetO));
            }
        } else {
            boolean caseSensitive = parentPair.isCaseSensitive();
            List<T> diffSourceList = new ArrayList<>(sourceC.size());
            Map<String,T> targetNameMap = byName(caseSensitive, targetC,keySupplier);
            
            mergedList = new ArrayList<SyncPair>(Math.max(sourceC.size(), targetC.size()));
            for (T source:sourceC) {
                String name = keySupplier.getName(source);
                T target = targetNameMap.remove(name);
                if (target!=null) {
                    mergedList.add(new SyncPair(parentPair, source, target));
                } else {
                    diffSourceList.add(source);
                }
            }
            
            Map<String,List<T>> sourceMap = groupByKey(caseSensitive, diffSourceList,keySupplier);
            Map<String,List<T>> targetMap = groupByKey(caseSensitive, targetNameMap.values(),keySupplier);
            for (Entry<String,List<T>> sourceE : sourceMap.entrySet()) {
                String key = sourceE.getKey();
                List<T> sourceL = sourceE.getValue();
                List<T> targetL = targetMap.remove(key);
                if (targetL == null) {
                    for (T sourceO:sourceL) {
                        mergedList.add(new SyncPair(parentPair, sourceO, null));
                    }
                } else if (sourceL.size() == 1 && targetL.size() == 1) {
                    mergedList.add(new SyncPair(parentPair, sourceL.get(0), targetL.get(0)));
                } else {
                    mergedList.addAll(keySupplier.resolveCollisions(parentPair, key, sourceL, targetL));
                }
            }
            for (List<T> targetL : targetMap.values()) {
                for (T targetO:targetL) {
                    mergedList.add(new SyncPair(parentPair, null, targetO));
                }
            }
        }
        return mergedList;
    }
    
    
    
    /**
     * Merges two lists of any objects by name provided by <code>namer</code> with keeping order.
     */
    // op      params         sort descr
    // reorder (index, index) any                                            0 - EQUALS, CHANGED, COPIED
    // insert  (index, index)  desc {correction for previous unselected}     1 - NEW
    // delete  (index, null)   any  {by name}                                2 - DELETED
    public List<SyncPair> mergeLists(SyncPair parentPair, List<?> sourceL, List<?> targetL, Namer namer) {
        List<SyncPair> mergedList;
        if (sourceL == null && targetL == null) {
            mergedList = new ArrayList<SyncPair>(0);
        } else if (targetL == null) {
            mergedList = new ArrayList<SyncPair>(sourceL.size());
            for (int i=0; i<sourceL.size(); ++i){
                SyncPair pair = new SyncPair(parentPair, sourceL.get(i), null);
                pair.setSourceIndex(i);
                mergedList.add(pair);
            }
        } else if (sourceL == null) {
            mergedList = new ArrayList<SyncPair>(targetL.size());
            for (int i=0; i<targetL.size(); ++i) {
                SyncPair pair = new SyncPair(parentPair, null, targetL.get(i));
                pair.setTargetIndex(i);
                mergedList.add(pair);
            }
        } else {
            List<Object> activeSource = new ArrayList<Object>(sourceL);
            int sourceSize = sourceL.size();
            int targetSize = targetL.size();
            
            Map<String, Integer> sourceMap = newNameMap(parentPair.isCaseSensitive(), sourceSize);
            for (int i=0; i<sourceSize; ++i) {
                Object sourceO = sourceL.get(i);
                String name = namer.getName(sourceO);
                if (sourceMap.put(name, Integer.valueOf(i)) != null) {
                    throw new SyncException("List already has " + namer.getType(sourceO) + " named " + name);
                }
            }
            mergedList = new ArrayList<SyncPair>(Math.max(sourceSize, targetSize));
            
            SyncPair pair;
            int i = 0;
            while (i<targetSize){
                Object targetItem = targetL.get(i);
                if (i == activeSource.size()){ // new (last item)
                    activeSource.add(targetItem);
                    pair = new SyncPair(parentPair, null, targetItem);
                    pair.setTargetIndex(i); // null, target
                    mergedList.add(pair);
                    i++;
                    continue;
                }
                
                Object sourceItem = activeSource.get(i);
                
                String sourceName = namer.getName(sourceItem);
                String targetName = namer.getName(targetItem);
                
                Integer sourceIndex = sourceMap.remove(targetName);
                if (sourceIndex == null){
                    // select add (insert new) or remove + set (replace with new)
                    // replace:        insert:
                    // 0 1 old 3       0 1  2  3
                    // 0 1 new 3       0 1 old 2 3
                    
                    int index = indexOf(i+1, sourceName, namer, targetL);
                    if (index == -1){ // item is not found into target (remove old, set new)
                        activeSource.set(i, targetItem); // set = remove(i)+add(i,targetItem)
                        // delete
                        pair = new SyncPair(parentPair, sourceItem, null);
                        index = sourceMap.remove(sourceName);
                        pair.setSourceIndex(index);
                        mergedList.add(pair);   // source, null
                        // new
                        pair = new SyncPair(parentPair, null, targetItem);
                        pair.setTargetIndex(i); // null, target
                    } else {
                        // new
                        activeSource.add(i, targetItem);
                        pair = new SyncPair(parentPair, null, targetItem);
                        pair.setTargetIndex(i); // null, target
                    }
                } else {
                    if (!targetName.equalsIgnoreCase(sourceName)){ // reorder/swap
                        int currentIndex = indexOf(i+1,targetName, namer, activeSource);
                        activeSource.add(i, activeSource.remove(currentIndex));
                        pair = new SyncPair(parentPair, activeSource.get(i), targetItem);
                    } else { // equals/none
                        pair = new SyncPair(parentPair, sourceItem, targetItem); // source, target
                    }
                    pair.setTargetIndex(i); // source, target
                    pair.setSourceIndex(sourceIndex);
                }
                mergedList.add(pair);
                i++;
            }
            while (i<activeSource.size()){ // delete
                Object sourceItem = activeSource.get(i);
                String sourceName = namer.getName(sourceItem);
                int index = sourceMap.remove(sourceName);

                pair = new SyncPair(parentPair, sourceItem, null); // source, null
                pair.setSourceIndex(index);
                mergedList.add(pair);
                i++;
            }
        }
        return mergedList;
    }
    
    private static int indexOf(int startIndex, String name, Namer namer, List<?> list){
        while (startIndex<list.size()){
            if (name.equalsIgnoreCase(namer.getName(list.get(startIndex)))){
                return startIndex;
            }
            startIndex++;
        }
        return -1;
    }

    public List<SyncAttributePair> mergeAttributes(Map<String, Object> sourceM, Map<String, Object> targetM) {
        return mergeAttributes(sourceM, targetM, false);
    }
    
    /**
     * Merges two map of attributes provided by key with case sensitive.
     */
    public List<SyncAttributePair> mergeAttributes(Map<String, Object> sourceM, Map<String, Object> targetM, boolean ignoreEquals) {
        List<SyncAttributePair> mergedList;
        if (sourceM == null && targetM == null) {
            mergedList = new ArrayList<SyncAttributePair>(0);
        } else if (targetM == null) {
            mergedList = new ArrayList<SyncAttributePair>(sourceM.size());
            for (Map.Entry<String, Object> entry : sourceM.entrySet()) {
                mergedList.add(new SyncAttributePair(entry.getKey(), entry.getValue(), null));
            }
        } else if (sourceM == null) {
            mergedList = new ArrayList<SyncAttributePair>(targetM.size());
            for (Map.Entry<String, Object> entry : targetM.entrySet()) {
                mergedList.add(new SyncAttributePair(entry.getKey(), null, entry.getValue()));
            }
        } else {
            Map<String, Object> targetMap = new HashMap<String, Object>(targetM);
            mergedList = new ArrayList<SyncAttributePair>(targetM.size());
            for (Map.Entry<String, Object> sourceEntry : sourceM.entrySet()) {
                String name = sourceEntry.getKey();
                Object target = targetMap.remove(name);
                SyncAttributePair syncAttr = new SyncAttributePair(name, sourceEntry.getValue(), target);
                if (!(ignoreEquals && syncAttr.getChangeType() == AttributeChangeType.EQUALS)) {
                    mergedList.add(syncAttr);
                }
            }
            for (Map.Entry<String, Object> targetEntry : targetMap.entrySet()) {
                String name = targetEntry.getKey();
                mergedList.add(new SyncAttributePair(name, null, targetEntry.getValue()));
            }
        }
        return mergedList;
    }
    
    /**
     * Default implementation compare by name.
     */
    @Override
    public float matchObjects(SyncPair source, SyncPair target, SyncSession session) {
        return source.getSourceName().equals(target.getTargetName()) ? 1 : 0;
    }

}
