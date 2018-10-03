package com.branegy.dbmaster.core;

import static com.branegy.dbmaster.core.Project.QUERY_PROJECT_BY_NAME;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.branegy.persistence.BaseEntity;

@Entity
@Access(AccessType.FIELD)
@Table(name = "Project")
@NamedQueries({
    @NamedQuery(name = QUERY_PROJECT_BY_NAME, query = "from Project p where p.name=:name")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Project extends BaseEntity {
    public static final String QUERY_PROJECT_BY_NAME = "Project.findByName";

    @Column(name = "name", unique = true, nullable = false, length = 255)
    @NotNull
    @Pattern(regexp = "^[^\\.]{1,255}$")
    private String name;

    @Column(name = "description", length = 255)
    @Size(max = 255)
    private String description;

    @Column(name = "projectType", length = 16, nullable = false)
    @NotNull
    @Size(min = 1, max = 16)
    @Pattern(regexp = "^(INVENTORY|MODELING)$", message = "Unsupported project type")
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
