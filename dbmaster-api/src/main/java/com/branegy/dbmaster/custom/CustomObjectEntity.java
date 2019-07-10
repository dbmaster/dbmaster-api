package com.branegy.dbmaster.custom;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="custom_object")
@Access(AccessType.FIELD)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@CustomFieldDiscriminator(CustomObjectEntity.CUSTOM_FIELD_DISCRIMINATOR)
@FetchAllObjectIdByProjectSql("SELECT co.id FROM custom_object co "
    + "WHERE co.object_type_id IN "
    + "(SELECT cot.id FROM custom_object_type cot WHERE cot.project_id = :projectId AND cot.clazz = :clazz")
public final class CustomObjectEntity extends BaseCustomEntity {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "CustomObject";
    
    @ManyToOne(optional = false, fetch=FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="object_type_id")
    @NotNull
    CustomObjectTypeEntity entityType;
    
    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }

    public final CustomObjectTypeEntity getEntityType() {
        return entityType;
    }

    public final void setEntityType(CustomObjectTypeEntity entityType) {
        this.entityType = entityType;
    }
}
