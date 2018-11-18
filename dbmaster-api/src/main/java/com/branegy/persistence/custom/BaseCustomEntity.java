package com.branegy.persistence.custom;

import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.branegy.persistence.BaseEntity;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseCustomEntity extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 500)
    private CustomFieldEntity custom;

    public final boolean hasCustomProperties() {
        return custom != null;
    }

    public Map<String, Object> getCustomMap() {
        if (custom == null) {
            custom = new CustomFieldEntity();
            custom.setClazz(getDiscriminator());
        }
        return custom.getMap();
    }

    @SuppressWarnings("unchecked")
    public <X> X getCustomData(String name) {
        return (X) getCustomMap().get(name);
    }

    public <X> void setCustomData(String name, X value) {
        if (value != null) {
            getCustomMap().put(name, value);
        } else if (custom!=null){
            getCustomMap().remove(name);
        }
    }

    public void setCustomMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            custom = null;
        } else {
            Map<String, Object> m = getCustomMap();
            m.clear();
            m.putAll(map);
        }
    }

    public String getDiscriminator() {
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

    /*public final boolean isCustomPropertiesEquals(BaseCustomEntity other){
        if (custom == null && other.custom == null){
            return true;
        } else if (custom == null){
            return other.custom.isEmpty();
        } else if (other.custom == null){
            return custom.isEmpty();
        } else {
            if (custom.isEmpty() && other.custom.isEmpty()){
                return true;
            } else if ((custom.isEmpty() && !other.custom.isEmpty()) ||
                       (!custom.isEmpty() && other.custom.isEmpty())){
                return false;
            } else {
                return custom.map.equals(other.custom.map);
            }
        }
    }*/

    CustomFieldEntity getCustom() {
        return custom;
    }
    
    protected String getDiscriminatorFromDatabase(){
        return custom.getClazz();
    }
    
    @Override
    public String toString() {
        return super.toString()+ (custom!=null?" "+custom:"");
    }
}
