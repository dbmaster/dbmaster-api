package com.branegy.persistence.custom;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.branegy.persistence.BaseEntity;

@MappedSuperclass
public abstract class BaseCustomEntity extends BaseEntity {
    public static final String CUSTOMFIELD_VALUE_TABLE  = "CUSTOMFIELD_VALUE";

    @Transient
    private List<EmbeddableObject> custom;
   
    @Transient
    private Map<String,Object> customMapView;
    
    
    protected final List<EmbeddableObject> getInnerCustomList(){
        return this.custom;
    }
    protected abstract List<EmbeddableObject> getCustom();
   
    // hibernate setter
    @SuppressWarnings("unused")
    private void setCustom(List<EmbeddableObject> list) {
        this.custom = list;
    }
   
    
    public final boolean hasCustomProperties() {
        return !custom.isEmpty();
    }

    public final void forEachCustomData(BiConsumer<String, Object> consumer) {
        if (hasCustomProperties()) {
            getCustomMap().forEach(consumer);
        }
    }

    public final void setCustomMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            getCustomMap().clear();
        } else {
            Map<String, Object> m = getCustomMap();
            m.clear();
            m.putAll(map);
        }
    }

    public final String getDiscriminator() {
        return getDiscriminator(getClass());
    }
    
    public static final String getDiscriminator(Class<? extends BaseCustomEntity> clazz){
        if (clazz == null) {
            throw new IllegalStateException("Clazz cannot be null");
        } else if (clazz == BaseCustomEntity.class){
            throw new IllegalArgumentException("Clazz should extend BaseCustomEntity");
        } else if (clazz.isAnnotationPresent(CustomFieldDiscriminator.class)) {
            CustomFieldDiscriminator annotation = clazz.getAnnotation(CustomFieldDiscriminator.class);
            assert annotation.value().length() > 0;
            return annotation.value();
        } else {
            throw new IllegalStateException("Add @" + CustomFieldDiscriminator.class.getSimpleName()
                    + " annotation to " + clazz.getName());
        }
    }

    @Override
    public String toString() {
        return super.toString()+ (custom!=null?" "+custom:"");
    }
    
    private void initViewMap() {
        if (customMapView == null) {
            if (custom == null) {
                custom = new ArrayList<>();
            }
            customMapView = new AbstractMap<String, Object>(){
                int size = -1;
                int modCount = 0;
                Set<Entry<String, Object>> viewSet;
                
                final void checkForComodification(int expectedModCount) {
                    if (modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                }
                
                private int calcSize() {
                    if (size == -1){
                        size = (int) custom.stream().map(EmbeddableObject::getFieldName).distinct().count();
                    }
                    return size;
                }
                
                private int binaryIndexOf(String key) {
                    int low = 0;
                    int high = custom.size()-1;
                    
                    while (low <= high) {
                        int mid = (low + high) >>> 1;
                        EmbeddableObject midVal = custom.get(mid);
                        int cmp = midVal.getFieldName().compareTo(key);
                        if (cmp < 0) {
                            low = mid + 1;
                        } else if (cmp > 0) {
                            high = mid - 1;
                        } else {
                            return mid; // key found
                        }
                    }
                    return -(low + 1);  // key not found
                }
                
                private int lower(int index) { // TODO n log n
                    EmbeddableObject object = custom.get(index);
                    String key = object.getFieldName();
                    int i=index;
                    while (i>0 && custom.get(i-1).getFieldName().equals(key)) {
                        i--;
                    }
                    return i;
                }
                
                private int upper(int index) { // TODO n log n
                    /*EmbeddableObject object = map.get(index);
                    String key = object.getFieldName();
                    
                    int low = index+1;
                    int high = Math.min(0xFF_FF-object.getValueOrder()+low, map.size())-1;
                    int mid = low;
                    while (low <= high) {
                        EmbeddableObject midVal = map.get(mid);
                        int cmp = midVal.getFieldName().compareTo(key);
                        if (cmp == 0) {
                            cmp = midVal.getValueOrder()-0;
                        }
                        
                        if (cmp < 0) {
                            low = mid + 1;
                        } else if (cmp > 0) {
                            high = mid - 1;
                        } else {
                            return mid; // key found
                        }
                        mid = (low + high) >>> 1;
                    }
                    return -(low + 1);  // key not found
                    */
                    
                    if (index >= custom.size()) {
                        return index;
                    }
                    
                    EmbeddableObject object = custom.get(index);
                    String key = object.getFieldName();
                    int i=index;
                    while (i<custom.size() && custom.get(i).getFieldName().equals(key)) {
                        i++;
                    }
                    return i;
                }
                
                private Object convertToObject(int fromINdex, int toIndex) {
                    if (fromINdex+1 == toIndex) {
                        return custom.get(fromINdex).getObject();
                    } else {
                        List<Object> result = new ArrayList<>(toIndex-fromINdex); // TODO collection view?
                        for (int i=fromINdex; i<toIndex; ++i) {
                            result.add(custom.get(i).getObject());
                        }
                        return Collections.unmodifiableList(result);
                    }
                }
                
                @Override
                public int size() {
                    return calcSize();
                }

                @Override
                public boolean isEmpty() {
                    return custom.isEmpty();
                }

                @Override
                public boolean containsValue(Object value) {
                    if (value == null) {
                        return false;
                    }
                    for (EmbeddableObject eo: custom) {
                        if (value.equals(eo.getObject())) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean containsKey(Object key) {
                    if (key == null || key.getClass()!=String.class) {
                        return false;
                    } 
                    return binaryIndexOf((String) key) >= 0;
                }

                @Override
                public Object get(Object key) {
                    if (key == null || key.getClass()!=String.class) {
                        return null;
                    } 
                    int index = binaryIndexOf((String) key);
                    if (index<0) {
                       return null; 
                    }
                    
                    int fromIndex = lower(index);
                    int toIndex = upper(index);
                    
                    return convertToObject(fromIndex, toIndex);
                }
                
                @Override
                public Object put(String key, Object value) {
                    if (value == null){
                        return remove(key);
                    }
                    
                    int index = binaryIndexOf(key);
                    modCount++;
                    if (index < 0) { // insert
                        size++;
                        String discriminator = getDiscriminator();
                        index = -(index+1);
                        if (value instanceof Collection<?>) {
                            Collection<?> c = (Collection<?>) value;
                            List<EmbeddableObject> newList = new ArrayList<>(c.size());
                            int i=0;
                            for (Object o:c) {
                                newList.add(new EmbeddableObject(key, i++, discriminator,o));
                            }
                            custom.addAll(index, newList);
                        } else {
                            custom.add(index, new EmbeddableObject(key, 0, discriminator,value));
                        }
                        return null;
                    } else { // update 
                        // TODO update index + merge
                        int from = lower(index);
                        int to = upper(index);
                        
                        Object oldValue = convertToObject(from, to);
                        List<EmbeddableObject> subList = custom.subList(from, to);
                        subList.clear();
                        String discriminator = getDiscriminator();
                        if (value instanceof Collection<?>) {
                            Collection<?> c = (Collection<?>) value;
                            List<EmbeddableObject> newList = new ArrayList<>(c.size());
                            int i=0;
                            for (Object o:c) {
                                newList.add(new EmbeddableObject(key, i++, discriminator,o));
                            }
                            custom.addAll(from, newList);
                        } else {
                            subList.add(from, new EmbeddableObject(key, 0, discriminator,value));
                        }
                        return oldValue;
                    }
                }
                
                @Override
                public Object remove(Object key) {
                    if (key == null || key.getClass()!=String.class) {
                        return null;
                    } 
                    int index = binaryIndexOf((String) key);
                    if (index < 0) {
                        return null;
                    }
                    
                    int from = lower(index);
                    int to = upper(index);
                    Object oldValue = convertToObject(from, to);
                    removeExists(from, to);
                    return oldValue;
                }

                private void removeExists(int fromIndex, int toIndex) {
                    if (fromIndex + 1 == toIndex){
                        custom.remove(fromIndex);
                    } else {
                        List<EmbeddableObject> subList = custom.subList(fromIndex, toIndex);
                        subList.clear();
                    }
                    modCount++;
                    --size;
                }
                
                @Override
                public void clear() {
                    modCount++;
                    custom.clear();
                }

                @Override
                public Set<Entry<String, Object>> entrySet() {
                    if (viewSet == null) {
                        viewSet = new AbstractSet<Map.Entry<String,Object>>() {
                            @Override
                            public Iterator<Entry<String, Object>> iterator() {
                                if (custom.isEmpty()) {
                                    return Collections.emptyIterator();
                                }
                                return new Iterator<Map.Entry<String,Object>>() {
                                    final int expectedModCount = modCount;
                                    // int status = unitinted, inited, removed
                                    int fromIndex = 0;
                                    int toIndex = upper(fromIndex);
                                    
                                    @Override
                                    public Entry<String, Object> next() {
                                        checkForComodification(expectedModCount);
                                        Object obj = convertToObject(fromIndex,toIndex);
                                        String key = custom.get(fromIndex).getFieldName();
                                        fromIndex = toIndex;
                                        toIndex = upper(toIndex);
                                        return new SimpleEntry<>(key, obj);
                                    }
                                    
                                    @Override
                                    public boolean hasNext() {
                                        checkForComodification(expectedModCount);
                                        return fromIndex < custom.size();
                                    }
                                    
                                    @Override
                                    public void remove() {
                                        /*checkForComodification(expectedModCount);
                                        if (fromIndex>=map.size()) {
                                            throw new IllegalStateException();
                                        }
                                        removeExists(fromIndex, toIndex);
                                        expectedModCount++;
                                        fromIndex = toIndex;
                                        toIndex = upper(toIndex);*/
                                        Iterator.super.remove(); // TODO implement
                                    }
                                };
                            }
                            @Override
                            public int size() {
                                return calcSize();
                            }
                        };
                    }
                    return viewSet;
                }
            }; 
        }
    }
    
    @SuppressWarnings("unchecked")
    public <X> X getCustomData(String key) {
        initViewMap();
        return (X) customMapView.get(key);
    }
    
    public void setCustomData(String key, Object value) {
        initViewMap();
        customMapView.put(key, value);
    }
    
    public Map<String,Object> getCustomMap(){
        initViewMap();
        return customMapView;
    }
}
