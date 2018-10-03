package com.branegy.dbmaster.custom;

import static com.branegy.dbmaster.custom.CustomObjectTypeEntity.QUERY_FIND_BY_PROJECT;
import static com.branegy.dbmaster.custom.CustomObjectTypeEntity.QUERY_FIND_BY_PROJECT_NAME;
import static com.branegy.dbmaster.custom.CustomObjectTypeEntity.QUERY_FIND_BY_PROJECT_WITH_KEY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.BaseEntity;

@Entity
@Table(name="custom_object_type", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "clazz"})
})
@NamedQueries({
    @NamedQuery(name=QUERY_FIND_BY_PROJECT, query="from CustomObjectTypeEntity c where c.project=:project "
            + "order by c.objectName asc"),
    @NamedQuery(name=QUERY_FIND_BY_PROJECT_NAME,
            query="from CustomObjectTypeEntity c where c.project=:project and objectName=:name"),
    @NamedQuery(name=QUERY_FIND_BY_PROJECT_WITH_KEY,
            query="select c, max(f.key) as pk from CustomFieldConfig f "
                    + "JOIN f.customObjectType c "
                    + "where c.project=:project "
                    + "group by c.id ")
})
/*@NamedNativeQueries({
    @NamedNativeQuery(name=QUERY_FIND_BY_PROJECT_WITH_KEY, query="select t.id,t.updated,t.created,"
            + "t.createAuthor,t.updateAuthor,t.clazz,t.project_id,t.create_support,t.update_support,"
            + "t.delete_support,t.hidden,j.name as pk "+
            "from CUSTOM_OBJECT_TYPE t "+
            "left join ( "+
            "  select c.CUSTOM_OBJECT_TYPE, c.name "+
            "  from CUSTOMFIELD_CONFIG c "+
            "  where c.project_id = :projectId and c.CUSTOM_OBJECT_TYPE is not null and c.KEY_FIELD = true "+
            ") as j on t.id = j.CUSTOM_OBJECT_TYPE "+
            "where t.project_id =:projectId",resultSetMapping=QUERY_FIND_BY_PROJECT_WITH_KEY)
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = QUERY_FIND_BY_PROJECT_WITH_KEY,
        columns = {
            @ColumnResult(name = "pk")},
        entities={
            @EntityResult(entityClass = CustomObjectTypeEntity.class,fields={
                @FieldResult(column = "id", name = "id"),
                @FieldResult(column = "updated", name = "updated"),
                @FieldResult(column = "created", name = "created"),
                @FieldResult(column = "createAuthor", name = "createAuthor"),
                @FieldResult(column = "updateAuthor", name = "updateAuthor"),
                @FieldResult(column = "clazz", name = "objectName"),
                @FieldResult(column = "project_id", name = "project"),
                @FieldResult(column = "create_support", name = "create"),
                @FieldResult(column = "update_support", name = "update"),
                @FieldResult(column = "delete_support", name = "delete"),
                @FieldResult(column = "hidden", name = "hidden")
            })
        }
    )
})*/
@CustomObjectTypeNameValidation(message = "This object type name is reserved.")
public class CustomObjectTypeEntity extends BaseEntity {
    public static final String QUERY_FIND_BY_PROJECT_NAME = "CustomObjectTypeEntity.findByProjectName";
    public static final String QUERY_FIND_BY_PROJECT = "CustomObjectTypeEntity.findByProject";
    public static final String QUERY_FIND_BY_PROJECT_WITH_KEY = "CustomObjectTypeEntity.findByProjectWithKey";

    @Column(name="clazz", nullable=false, length = 32)
    @NotNull
    @Size(min=1,max=32)
    String objectName;
    
    @Column(name="tab_title", length = 32)
    @Size(min=1,max=32)
    String tabTitle;
    
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    @NotNull
    Project project;
    
    @Column(name="create_support", nullable=false)
    boolean create;
    
    @Column(name="update_support", nullable=false)
    boolean update;
    
    @Column(name="delete_support", nullable=false)
    boolean delete;
    
    @Column(name="hidden", nullable=false)
    boolean hidden;
    
    @Column(name="system", updatable=false)
    boolean system;
    
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
