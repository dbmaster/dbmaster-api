package com.branegy.dbmaster.custom;

import static com.branegy.dbmaster.custom.CustomObjectEntity.QUERY_DELETE_BY_CLAZZ_PROJECT;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="custom_object")
@Access(AccessType.FIELD)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select distinct co.id from custom_object co "+
    "inner join CustomFieldEntity cfe on co.CUSTOM_ID = cfe.id "+
    "where co.project_id = :projectId and cfe.clazz = :clazz")
@NamedQueries({
    @NamedQuery(name=QUERY_DELETE_BY_CLAZZ_PROJECT,
            query="delete from CustomObjectEntity e "
                + "where e in ("
                        + "select o from CustomObjectEntity o inner join o.custom c "
                        + "where c.clazz =:clazz and o.project=:project"
                + ")")
})
public final class CustomObjectEntity extends BaseCustomEntity {
    public static final String QUERY_DELETE_BY_CLAZZ_PROJECT = "CustomObjectEntity.deleteByClazzProject";

    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    @NotNull
    Project project;
    
    @Transient
    String discriminator;

    @Override
    public String getDiscriminator() {
        if (hasCustomProperties() && isPersisted()){
            return getDiscriminatorFromDatabase();
        }
        if (discriminator == null){
            throw new IllegalStateException("Discriminator is not set");
        }
        return discriminator;
    }
    
    public void setDiscriminator(String discriminator){
        if ((hasCustomProperties() && isPersisted()) || this.discriminator!=null){
            throw new IllegalStateException("Discriminator is already set");
        }
        this.discriminator = discriminator;
    }
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
