package com.branegy.dbmaster.model;

import static com.branegy.dbmaster.model.ModelObject.QUERY_FIND_BY_NAME;
import static com.branegy.dbmaster.model.ModelObject.QUERY_FIND_PAGE;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import com.google.common.base.Preconditions;

import com.branegy.service.core.exception.IllegalStateApiException;

@Entity
@Table(name="db_model_object")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING,length=5)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@BatchSize(size=100)
@NamedQueries({
    @NamedQuery(name=QUERY_FIND_PAGE, query="select count(mo.id) from ModelObject mo " +
            "where mo.model.id=:modelId and (TYPE(mo)<:type or (TYPE(mo)=:type and " +
            "UPPER(mo.name)<UPPER(:name)))"),
    @NamedQuery(name=QUERY_FIND_BY_NAME, query="select mo from ModelObject mo " +
            "where mo.model.id=:modelId and TYPE(mo)=:type and UPPER(mo.name)=UPPER(:name)")
})
public abstract class ModelObject extends DatabaseObject<Model> {
    public static final String QUERY_FIND_PAGE = "ModelObject.findPage";
    public static final String QUERY_FIND_BY_NAME = "ModelObject.findByName";
    
    @ManyToOne(optional=false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="model_id")
    Model model;

    @javax.persistence.Column(name="name",length=255)
    @Size(min=1,max=255)
    String name;

    @OneToMany(mappedBy="owner",targetEntity=Column.class, orphanRemoval=true,
            cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause="DTYPE = 'Column'")
    @OrderBy("collectionIndex ASC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)
    List<Column> columns;

    @OneToMany(mappedBy="owner",targetEntity=Column.class, orphanRemoval=true,
            cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause="DTYPE = 'Parameter'")
    @OrderBy("collectionIndex ASC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)
    List<Parameter> parameters;

    @OneToMany(mappedBy="owner",targetEntity=Index.class, orphanRemoval=true,
            cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)
    List<Index> indexes;

    @OneToMany(mappedBy="owner",targetEntity=Constraint.class, orphanRemoval=true,
            cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)
    List<Constraint> constraints;

    @OneToMany(mappedBy="owner",targetEntity=ForeignKey.class, orphanRemoval=true,
            cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)
    List<ForeignKey> foreignKeys;

    /*@OneToMany(mappedBy="owner",targetEntity=Trigger.class, orphanRemoval=true,
            cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size=100)*/
    @Transient
    List<Trigger> triggers;
    
    public boolean isSchemaObject(){
        return name.indexOf('.')!=-1;
    }

    public String getSchema() {
        int index = name.indexOf('.');
        if (index==-1){
            return null;
        } else {
            return name.substring(0,index);
        }
    }

    public String getSimpleName() {
        return name.substring(name.indexOf('.')+1);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    <T extends Column> void removeChildColumn(List<T> list, T child, String parameterName) {
        removeChild(list, child, parameterName);
        updateCollectionOrder(list);
    }

    <T extends Column> List<T> insert(List<T> list, T child, int index, String parameterName){
        Preconditions.checkNotNull(child, "Parameter "+parameterName+" can not be null");
        if (list == null){
            list = new ArrayList<T>();
        }
        
        int existsIndex = indexOfByName(list, child.getName());
        if (existsIndex == -1){ // not found, insert
            child.setParent(this);
            
            if (index > list.size()){
                index = list.size(); // correction index (insert last)
            }
            
            if (index < 0){
                list.add(child);
            } else {
                list.add(index, child);
            }
        } else if (existsIndex!=index) {
            if (list.get(existsIndex) != child){
                throw new IllegalStateApiException("Child element is not in the list, "
                        + "but some item exits with name "+child.getName());
            }
            
            if (index > list.size()){
                index = list.size()-1; // correction index (insert last)
            }
            
            list.add(index, list.remove(existsIndex));
        }
        updateCollectionOrder(list);
        return list;
    }
    
    final <T extends Column> List<T> mergeColumnList(List<T> oldList,List<T> newList){
        List<T> mergeList = mergeList(oldList, newList);
        updateCollectionOrder(mergeList);
        return mergeList;
    }
    
    private int indexOfByName(List<? extends Column> list, String name){
        for (int i=0; i<list.size(); ++i){
            if (name.equalsIgnoreCase(list.get(i).getName())){
                return i;
            }
        }
        return -1;
    }
    
    @PrePersist
    @PreUpdate
    public final void updateCollectionOrder(){
        updateCollectionOrder(columns);
        updateCollectionOrder(parameters);
    }
    
    static final void updateCollectionOrder(List<? extends Column> columns){
        if (columns!=null){
            int i = 0;
            for (Column p:columns){
                if (p.collectionIndex<i){
                    p.collectionIndex = i;
                    i++;
                } else if (i==Integer.MAX_VALUE){
                    for (i=0; i<columns.size(); ++i){
                        columns.get(i).collectionIndex = i;
                    }
                    break;
                } else {
                    i = p.collectionIndex+1;
                }
            }
        }
    }
    
    final <T extends Column> List<T> listColumnProxy(List<T> field){
        return new ColumnListProxy<T>(field, this);
    }

    @Override
    final void setParent(Model parent) {
        this.model = parent;
    }

    @Override
    final Model getParent() {
        return model;
    }
}
