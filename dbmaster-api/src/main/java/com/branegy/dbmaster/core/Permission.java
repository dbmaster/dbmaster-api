package com.branegy.dbmaster.core;

import static com.branegy.dbmaster.core.Permission.QUERY_PERMISSION_BY_PROJECT;
import static com.branegy.dbmaster.core.Permission.QUERY_PERMISSION_BY_PROJECT_USER;
import static com.branegy.dbmaster.core.Permission.QUERY_PERMISSION_BY_USER;
import static com.branegy.dbmaster.core.Permission.QUERY_PERMISSION_COUNT_BY_PROJECT;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.persistence.BaseEntity;

@Entity
@Access(AccessType.FIELD)
@Table(name="Permission",
    uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "project_id", "role" }) })
@NamedQueries( {
        @NamedQuery(name = QUERY_PERMISSION_BY_PROJECT_USER, query = "from Permission p "
                + "where p.project=:project and (p.user=:user or p.user is null)",
                    hints = @QueryHint(name = HINT_CACHEABLE, value="true")
        ),
        @NamedQuery(name = QUERY_PERMISSION_BY_USER, query = "from Permission p "
                + "where p.user.id=:userId"),
        @NamedQuery(name = QUERY_PERMISSION_BY_PROJECT, query = "from Permission p "
                + "where p.project.id=:projectId"),
        @NamedQuery(name = QUERY_PERMISSION_COUNT_BY_PROJECT, query = "select count(p) from Permission p "
                + "where p.project.id=:projectId")
})
public class Permission extends BaseEntity {
    public static final String QUERY_PERMISSION_BY_PROJECT = "Permission.findByProject";
    public static final String QUERY_PERMISSION_COUNT_BY_PROJECT = "Permission.findCountByProject";
    public static final String QUERY_PERMISSION_BY_USER = "Permission.findByUser";
    public static final String QUERY_PERMISSION_BY_PROJECT_USER = "Permission.findByProjectUser";
 
    public static enum Role {
        FULL_CONTROL, CONTRIBUTOR, READONLY
    }

    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    protected Project project;

    @ManyToOne
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="user_id")
    protected User user;

    @Enumerated(EnumType.STRING)
    @Column(name="role",nullable = false,length=12)
    protected Role role;

    public Permission() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
