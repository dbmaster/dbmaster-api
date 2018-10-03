package com.branegy.persistence.custom;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.branegy.persistence.CurrentUserService;

@Entity
@BatchSize(size=500)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name="CustomFieldEntity")
class CustomFieldEntity {
    private static final int MAX_AUTHOR_SIZE = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id",updatable=false)
    long id;

    @ElementCollection(fetch=FetchType.EAGER)
    @MapKeyColumn(name = "MAP_KEY",length=64)
    @BatchSize(size=1000)
    @CollectionTable(name="CUSTOMFIELDENTITY_MAP",joinColumns = {@JoinColumn(name="CUSTOMFIELDENTITY_ID")})
    Map<String, PrimitiveTypeWrapper> map;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="updated",nullable = false)
    private Date updated;
    @Column(name="updateAuthor",length = 15)
    @Size(min = 1, max = MAX_AUTHOR_SIZE)
    private String updateAuthor;
    @Column(name="clazz",nullable = false, updatable = false, length = 32)
    private String clazz;

    @PrePersist
    @PreUpdate
    final void preUpdateInfo() {
        updated = new Date();
        updateAuthor = CurrentUserService.getCurrentUser(MAX_AUTHOR_SIZE);
    }

    @Transient
    transient CustomMapWrapper mapWrapper;

    Map<String, Object> getMap() {
        if (map == null) {
            map = new HashMap<String, PrimitiveTypeWrapper>();
        }
        if (mapWrapper == null) {
            mapWrapper = new CustomMapWrapper();
        }
        return mapWrapper;
    }

    /**
     * @author keygen
     * Map<String,PrimitiveTypeWrapper> -> Map<String,Object>
     */
    private class CustomMapWrapper implements Map<String, Object> {

        protected Object toTarget(Object object) {
            if (object == null) {
                return null;
            } else {
                return ((PrimitiveTypeWrapper) object).getObject();
            }
        }

        protected PrimitiveTypeWrapper toSource(Object object) {
            if (PrimitiveTypeWrapper.isSupportWrap(object)) {
                return new PrimitiveTypeWrapper(object);
            } else {
                throw new IllegalArgumentException("" + object);
            }
        }

        private SetProxy<Entry<String, PrimitiveTypeWrapper>, Entry<String, Object>> set;

        private CollectionProxy<PrimitiveTypeWrapper, Object> values;

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public boolean containsKey(Object paramObject) {
            return map.containsKey(paramObject);
        }

        @Override
        public boolean containsValue(Object paramObject) {
            if (PrimitiveTypeWrapper.isSupportWrap(paramObject)) {
                return map.containsValue(new PrimitiveTypeWrapper(paramObject));
            } else {
                return false;
            }
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            if (set == null) {
                set = new SetProxy<Entry<String, PrimitiveTypeWrapper>,
                Entry<String, Object>>(map.entrySet()) {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected Entry<String, PrimitiveTypeWrapper> toSource(Object object) {
                        Entry<String, Object> e = (Entry<String, Object>) object;
                        return new EntryImpl<String, PrimitiveTypeWrapper>(e.getKey(), CustomMapWrapper.this
                                .toSource(e.getValue()));
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    protected Map.Entry<String, Object> toTarget(Object object) {
                        return new EntryProxy<String, PrimitiveTypeWrapper, Object>(
                                (Entry<String, PrimitiveTypeWrapper>) object) {
                            @Override
                            protected PrimitiveTypeWrapper toSource(Object object) {
                                return CustomMapWrapper.this.toSource(object);
                            }

                            @Override
                            protected Object toTarget(Object object) {
                                return CustomMapWrapper.this.toTarget(object);
                            }
                        };
                    }
                };
            }
            return set;
        }

        @Override
        public Object get(Object paramObject) {
            return toTarget(map.get(paramObject));
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return map.keySet();
        }

        @Override
        public Object put(String paramK, Object paramV) {
            if (paramV!=null && !"".equals(paramV)){ // skip null and empty string
                return map.put(paramK, new PrimitiveTypeWrapper(paramV));
            } else {
                return map.remove(paramK);
            }
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> paramMap) {
            Map<String, PrimitiveTypeWrapper> m = new HashMap<String, PrimitiveTypeWrapper>();
            for (Entry<? extends String, ? extends Object> e : paramMap.entrySet()) {
                if (e.getValue()!=null && !"".equals(e.getValue())){ // skip null and empty string
                    m.put(e.getKey(), toSource(e.getValue()));
                } else {
                    map.remove(e.getKey());
                }
            }
            map.putAll(m);
        }

        @Override
        public Object remove(Object paramObject) {
            return toTarget(map.remove(paramObject));
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public Collection<Object> values() {
            if (values == null) {
                values = new CollectionProxy<PrimitiveTypeWrapper, Object>(map.values()) {
                    @Override
                    protected Object toTarget(Object object) {
                        return CustomMapWrapper.this.toTarget(object);
                    }

                    @Override
                    protected PrimitiveTypeWrapper toSource(Object object) {
                        return CustomMapWrapper.this.toSource(object);
                    }
                };
            }
            return values;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Map<?, ?>)) {
                return false;
            }
            Map<?, ?> localMap = (Map<?, ?>) obj;
            if (localMap.size() != size())
                return false;
            try {
                Iterator<?> it = entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<?, ?> e = (Map.Entry<?, ?>) it.next();
                    Object k = e.getKey();
                    Object v = e.getValue();
                    if (v == null) {
                        if ((localMap.get(k) != null) || (!(localMap.containsKey(k)))) {
                            return false;
                        }
                    } else if (!(v.equals(localMap.get(k)))) {
                        return false;
                    }
                }
            } catch (ClassCastException localClassCastException) {
                return false;
            } catch (NullPointerException localNullPointerException) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }

        @Override
        public String toString() {
            Iterator<Entry<String, PrimitiveTypeWrapper>> it = map.entrySet().iterator();
            if (!(it.hasNext())){
                return "{}";
            }
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append('{');
            while (true) {
                Entry<String, PrimitiveTypeWrapper> localEntry = it.next();
                Object key = localEntry.getKey();
                Object val = toTarget(localEntry.getValue());
                strBuilder.append(key);
                strBuilder.append('=');
                strBuilder.append((val == this) ? "(this Map)" : val);
                if (!(it.hasNext())) {
                    return strBuilder.append('}').toString();
                }
                strBuilder.append(", ");
            }
        }
    }

    @Override
    public String toString() {
        return clazz+": "+(map==null?"":map.toString());
    }

    public Date getUpdated() {
        return updated;
    }

    public String getUpdateAuthor() {
        return updateAuthor;
    }

    public String getClazz() {
        return clazz;
    }

    void setClazz(String clazz) {
        this.clazz = clazz;
    }

    /*boolean isEmpty(){
        return map==null || map.isEmpty();
    }*/

    public long getId() {
        return id;
    }
}
