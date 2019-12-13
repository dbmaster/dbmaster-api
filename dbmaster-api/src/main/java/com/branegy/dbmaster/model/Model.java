package com.branegy.dbmaster.model;

import static com.branegy.dbmaster.model.Model.QUERY_MODEL_BY_PROJECT;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_BY_PROJECT_NAME;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_BY_PROJECT_NAME_SEARCH;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_COUNT_BY_PROJECT;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_COUNT_BY_PROJECT_NAME;
import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;

import java.util.List;
import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name = "db_model", uniqueConstraints={
    @UniqueConstraint(columnNames = {"project_id","name"})
})
@CustomFieldDiscriminator(Model.CUSTOM_FIELD_DISCRIMINATOR)
/*@NamedQueries({
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT_USER,
            query = "select m from com.branegy.dbmaster.model.Model m, com.branegy.dbmaster.core.Permission p "
                  + "where m.project.id=:projectId and p.project.id=m.project.id and (p.user.id=:userId or :userAdmin=:true)"
                  + "order by m.name asc"),
    @NamedQuery(name = QUERY_MODEL_COUNT_BY_PROJECT_USER,
            query = "select count(m) from com.branegy.dbmaster.model.Model m, com.branegy.dbmaster.core.Permission p "
                    + "where m.project.id=:projectId and p.project.id=m.project.id and (p.user.id=:userId or :userAdmin=:true)"
                    + "order by m.name asc"),
})*/
@NamedQueries({
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT,
            query = "from Model m where m.project.id=:projectId order by m.name asc"),
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT_NAME,
            query = "from Model m where m.project.id=:projectId and m.name=:name"),
    
    
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT_NAME_SEARCH,
            query = "FROM Model m WHERE m.project.id=:projectId "/*AND (m.name LIKE :name OR ("
                    + ""
                    + "))"*/),
    
    
    @NamedQuery(name = QUERY_MODEL_COUNT_BY_PROJECT,
            query = "select count(m) from Model m where m.project.id=:projectId"),
    @NamedQuery(name = QUERY_MODEL_COUNT_BY_PROJECT_NAME,
            query = "select count(m) from Model m where m.project.id=:projectId and m.name<:name")
})

@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from db_model where project_id=:projectId")
public class Model extends BaseCustomEntity {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Model";
    
    public static final String QUERY_MODEL_BY_PROJECT = "Model.findByProjectId";
    public static final String QUERY_MODEL_BY_PROJECT_NAME = "Model.findByProjectName";
    public static final String QUERY_MODEL_BY_PROJECT_NAME_SEARCH = "Model.findByProjectNameSearch";
    
    public static final String QUERY_MODEL_COUNT_BY_PROJECT = "Model.findCountByProjectId";
    public static final String QUERY_MODEL_COUNT_BY_PROJECT_NAME = "Model.findCountByProjectIdName";
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;

    @Column(name="name",length=255, nullable = false)
    @Size(min=1,max=255)
    @NotNull
    String name;
    
    @Column(name="readonly_flag", nullable = false)
    boolean readonlyConfig;

    @OneToMany(mappedBy = "model", targetEntity = ModelDataSource.class, 
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @OrderBy("order")
    List<ModelDataSource> dataSources;
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isReadonly() {
        return readonlyConfig;
    }

    public void setReadonly(boolean readonlyConfig) {
        this.readonlyConfig = readonlyConfig;
    }
    
    public List<ModelDataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<ModelDataSource> dataSources) {
        this.dataSources = dataSources;
        dataSources.forEach(ds->{ds.setModel(this);});
    }

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
}
