package com.branegy.tools.model;

import static com.branegy.tools.model.ToolPermission.CHECK_EXECUTE_ACCESS;
import static com.branegy.tools.model.ToolPermission.CHECK_VIEW_HISTORY_ACCESS;
import static com.branegy.tools.model.ToolPermission.EXECUTION_ACCESS_LIST_FOR_USER_PROJECT;
import static com.branegy.tools.model.ToolPermission.EXECUTION_ACCESS_LIST_FOR_USER_PROJECT_SQL;

import java.util.EnumSet;
import java.util.Set;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;
import com.branegy.persistence.BaseEntity;

@Entity
@Table(name="dbm_tool_permission", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tool_id", "user_id","project_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@NamedQueries({
    @NamedQuery(name = CHECK_EXECUTE_ACCESS, query =
        "select COALESCE(MAX(CASE WHEN (tp.execute = true) THEN 1 ELSE 0 END),0) as access_execute " +
            "from ToolPermission tp " +
        "where tp.project=:project and tp.toolId=:toolId and (tp.user=:user or tp.user is null)"),
    @NamedQuery(name = CHECK_VIEW_HISTORY_ACCESS, query =
        "select COALESCE(MAX(CASE WHEN (tp.viewHistory = true) THEN 1 ELSE 0 END),0) as access_view_history "+
            "from ToolPermission tp " +
        "where tp.project=:project and tp.toolId=:toolId and (tp.user=:user or tp.user is null)"),
    @NamedQuery(name = EXECUTION_ACCESS_LIST_FOR_USER_PROJECT,
        query = EXECUTION_ACCESS_LIST_FOR_USER_PROJECT_SQL)
})
public class ToolPermission extends BaseEntity{
    public static final String EXECUTION_ACCESS_LIST_FOR_USER_PROJECT_SQL =
            "select tp.toolId from ToolPermission tp " +
            "where tp.project=:project and (tp.user=:user or tp.user is null) "+
            "group by tp.toolId " +
            "having COALESCE(MAX(CASE WHEN (tp.execute = true) THEN 1 ELSE 0 END),0)=1";
    public static final String EXECUTION_ACCESS_LIST_FOR_USER_PROJECT =
            "ToolPermission.executionAccessListForUserProject";
    public static final String CHECK_VIEW_HISTORY_ACCESS = "ToolPermission.checkViewHistoryAccess";
    public static final String CHECK_EXECUTE_ACCESS = "ToolPermission.checkExecuteAccess";

    public static enum ToolAccess {
        VIEW_HISTORY, EXECUTE;
    }
    
    @Column(name="tool_id",nullable = false,length=64,updatable=false)
    @NotNull
    @Size(max = 64)
    String toolId;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="user_id")
    User user;
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id", updatable = false)
    @NotNull
    Project project;
    
    @Column(name="access_view_history")
    boolean viewHistory;
    @Column(name="access_execute")
    boolean execute;

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    
    public void setAccessSet(Set<ToolAccess> acessSet){
        viewHistory = acessSet.contains(ToolAccess.VIEW_HISTORY);
        execute = acessSet.contains(ToolAccess.EXECUTE);
    }
    
    public Set<ToolAccess> getAccessSet(){
        Set<ToolAccess> set = EnumSet.noneOf(ToolAccess.class);
        if (viewHistory){
            set.add(ToolAccess.VIEW_HISTORY);
        }
        if (execute){
            set.add(ToolAccess.EXECUTE);
        }
        return set;
    }
    
    public boolean isAllowed(ToolAccess access){
        return (access == ToolAccess.EXECUTE && execute) || (access ==ToolAccess.VIEW_HISTORY && viewHistory);
    }
}
