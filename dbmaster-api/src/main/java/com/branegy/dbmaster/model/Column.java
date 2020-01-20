package com.branegy.dbmaster.model;

import static com.branegy.dbmaster.model.Column.QUERY_FIND_ALL_BY_MODELNAME;
import static com.branegy.dbmaster.model.Column.QUERY_FIND_ALL_COUNT_BY_MODELNAME;
import static com.branegy.dbmaster.model.Column.QUERY_FIND_ALL_COUNT_LESS_BY_MODELNAME;
import static com.branegy.dbmaster.model.Column.QUERY_FIND_BY_MODELOBJECTNAME;
import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="db_column")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING,length=31) //TODO fix length
@DiscriminatorValue(Column.CUSTOM_FIELD_DISCRIMINATOR)
@CustomFieldDiscriminator(Column.CUSTOM_FIELD_DISCRIMINATOR)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedQueries({
    @NamedQuery(name=QUERY_FIND_BY_MODELOBJECTNAME, query="from Column c where " +
            "c.owner.id=:modelObjectId and UPPER(c.name)=UPPER(:name)"),
    @NamedQuery(name=QUERY_FIND_ALL_COUNT_LESS_BY_MODELNAME, query="select count(c.id) " +
            "from Column c " +
            "where c.owner.datasource.id=:modelId and " +
            "(UPPER(CONCAT(c.owner.name,'.',c.name))<UPPER(:name) or " +
            "(UPPER(CONCAT(c.owner.name,'.',c.name))=UPPER(:name) and TYPE(c.owner)<:type))"),
    @NamedQuery(name=QUERY_FIND_ALL_BY_MODELNAME, query="from Column c " +
            "where c.owner.datasource.id=:modelId " +
            "order by CONCAT('.',c.owner.name,'.',c.name) asc,TYPE(c.owner) asc"),
    @NamedQuery(name=QUERY_FIND_ALL_COUNT_BY_MODELNAME,
            query="select count(c.id) from Column c where c.owner.datasource.id=:modelId")
})
@FetchAllObjectIdByProjectSql("select c.id from db_column c "+
        "inner join db_model_object mo on c.owner_id = mo.id "+
        "inner join db_model_datasource m on m.id = mo.model_id "+
        "where m.project_id = :projectId and dtype='Column'")
public class Column extends DatabaseObject<ModelObject> {
    public static final String CUSTOM_FIELD_DISCRIMINATOR = "Column";
    public static final String QUERY_FIND_BY_MODELOBJECTNAME = "Column.findByModelObjectName";
    public static final String QUERY_FIND_ALL_COUNT_LESS_BY_MODELNAME =
            "Column.findAllCountLessByModelIdName";
    public static final String QUERY_FIND_ALL_COUNT_BY_MODELNAME = "Column.findAllCountByModelId";
    public static final String QUERY_FIND_ALL_BY_MODELNAME = "Column.findAllByModelId";
    private static final int MAX_LENGTH = -1;

    public static final String DESCRIPTION = "Description";

    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="owner_id")
    ModelObject owner;

    @javax.persistence.Column(name="name",length=255)
    @Size(min=1,max=255)
    String name;

    @javax.persistence.Column(name="type",length=255)
    @Size(min=1,max=255)
    String type;

    @javax.persistence.Column(name="nullable")
    boolean nullable;

    /**
     * Maximum length of the column
     */
    @javax.persistence.Column(name="size")
    Integer size;

    /**
     * The maximum total number of decimal digits that can be stored, both to
     * the left and to the right of the decimal point.
     */
    // TODO Rename to precision
    @javax.persistence.Column(name="precesion")
    Integer precesion;

    /**
     * FOR SQL Server:The maximum number of decimal digits that can be stored to the right of
     * the decimal point. Scale must be a value from 0 through p. Scale can be specified only
     * if precision is specified. The default scale is 0; therefore, 0 <= s <= p
     */
    @javax.persistence.Column(name="scale")
    Integer scale;

    @javax.persistence.Column(name="defaultValue",length=255)
    @Size(max=255)
    String defaultValue;

    @javax.persistence.Column(name="extraDefinition",length=1024)
    @Size(max=1024)
    String extraDefinition;

    @javax.persistence.Column(name="collection_index")
    int collectionIndex;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPrecesion() {
        return precesion;
    }

    public void setPrecesion(Integer precesion) {
        this.precesion = precesion;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getExtraDefinition() {
        return extraDefinition;
    }

    public void setExtraDefinition(String extraDefinition) {
        this.extraDefinition = extraDefinition;
    }

    public ModelObject getOwner() {
        return owner;
    }

    void setOwner(ModelObject owner) {
        this.owner = owner;
    }

    // TODO (Slava) Move logic to dialect
    public boolean isBLOB() {
        return getType().equals("text")
                || getType().equals("ntext")
                || getType().equals("image")
                || (getType().equals("varchar") && getSize()==MAX_LENGTH);
    }

    public String getPrettyType() {
        if (getPrecesion() != null && getScale()!=null) {
            return getType() + " (" + getPrecesion() + ", " + getScale() + ")";
        } else if (getPrecesion() != null ) {
            return getType() + " (" + getPrecesion() + ")";
        } else if (getSize() != null) {
            return getType() + " (" + (getSize() == MAX_LENGTH ? "max" : getSize()) + ")";
        } else {
            return getType();
        }
    }

    public int getCollectionIndex() {
        return collectionIndex;
    }

    @Override
    final void setParent(ModelObject parent) {
        this.owner = parent;
    }

    @Override
    final ModelObject getParent() {
        return owner;
    }

    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    // (workaround) hibernate can't override property mapping in subclass. 
    @Where(clause="("+CLAZZ_COLUMN+"='"+Column.CUSTOM_FIELD_DISCRIMINATOR+"' or "+
                      CLAZZ_COLUMN+"='"+Parameter.CUSTOM_FIELD_DISCRIMINATOR+"'"
                + ")")
    @SortNatural
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }
}
