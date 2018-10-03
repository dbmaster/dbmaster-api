package com.branegy.tools.model;

import static com.branegy.tools.model.AdhocReportConfig.QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL_WITH_SECURITY;
import static com.branegy.tools.model.AdhocReportConfig.QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL;
import static com.branegy.tools.model.AdhocReportConfig.QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL_SHORTCUT;
import static com.branegy.tools.model.AdhocReportConfig.QUERY_COUNT_ADHOC_FIND_BY_USER_PROJECT_TOOL;
import static com.branegy.tools.model.AdhocReportConfig.QUERY_COUNT_ADHOC_FIND_BY_USER_PROJECT_TOOL_WITH_SECURITY;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;
import com.branegy.persistence.BaseEntity;

@Entity
@Access(AccessType.FIELD)
@Table(name = "adhoc_report_config",uniqueConstraints = {
    @UniqueConstraint(columnNames = {"report_name", "user_id" })
})
@NamedQueries({
    @NamedQuery(name=QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL,query="from AdhocReportConfig a where " +
            "(a.user is null or a.user=:user) and a.project=:project and a.baseReportId=:toolId " +
            "order by UPPER(a.reportName) asc"),
    @NamedQuery(name=QUERY_COUNT_ADHOC_FIND_BY_USER_PROJECT_TOOL,
        query="select count(a) from AdhocReportConfig a where " +
            "(a.user is null or a.user=:user) and a.project=:project and a.baseReportId=:toolId"),
    @NamedQuery(name=QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL_WITH_SECURITY,
        query="from AdhocReportConfig a where " +
            "(a.user is null or a.user=:user) and a.project=:project and a.baseReportId=:toolId " +
            "and a.parentToolId in ("+ ToolPermission.EXECUTION_ACCESS_LIST_FOR_USER_PROJECT_SQL +") " +
            "order by UPPER(a.reportName) asc"),
    @NamedQuery(name=QUERY_COUNT_ADHOC_FIND_BY_USER_PROJECT_TOOL_WITH_SECURITY,
        query="select count(a) from AdhocReportConfig a where " +
            "(a.user is null or a.user=:user) and a.project=:project and a.baseReportId=:toolId "+
            "and a.parentToolId in ("+ ToolPermission.EXECUTION_ACCESS_LIST_FOR_USER_PROJECT_SQL +")"),
    @NamedQuery(name=QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL_SHORTCUT,query="from AdhocReportConfig a where " +
            "(a.user is null or a.user.id=:userId) and a.project.id=:projectId " +
            "and UPPER(a.baseReportId)=UPPER(:toolId) and UPPER(a.reportName)=UPPER(:shortcut)")
})
public class AdhocReportConfig extends BaseEntity {
    public static final String QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL =
            "AdhocReportConfig.findByUserProjectTool";
    public static final String QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL_WITH_SECURITY =
            "AdhocReportConfig.findByUserProjectToolWithSecurity";
    public static final String QUERY_COUNT_ADHOC_FIND_BY_USER_PROJECT_TOOL =
            "AdhocReportConfig.countByUserProjectTool";
    public static final String QUERY_COUNT_ADHOC_FIND_BY_USER_PROJECT_TOOL_WITH_SECURITY =
            "AdhocReportConfig.countByUserProjectToolWithSecurity";
    public static final String QUERY_ADHOC_FIND_BY_USER_PROJECT_TOOL_SHORTCUT =
            "AdhocReportConfig.findByUserProjectToolShortcut";

    @Column(name="base_report",nullable=false,length=64)
    private String baseReportId;
    
    @Column(name="parent_tool_id",nullable=false,length=64)
    private String parentToolId;

    @Column(name="report_name",nullable=false,length=128)
    @NotNull
    @Pattern(regexp = "^[^/]{1,128}$")
    private String reportName;

    @Column(name="report_description",length=255)
    @Size(max = 255)
    private String reportDescription;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="project_id")
    private Project project;
    
    @Column(name="parameters",nullable=false)
    @Lob
    private String parameters;

    public String getBaseReportId() {
        return baseReportId;
    }
    public void setBaseReportId(String baseReportId) {
        this.baseReportId = baseReportId;
    }
    public String getReportName() {
        return reportName;
    }
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    public String getReportDescription() {
        return reportDescription;
    }
    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }
    public String getParameters() {
        return parameters;
    }
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    // workaround, will be fixed in 1.8?
    @SuppressWarnings("unchecked")
    public Map<String,Object> getParameterMap(){
        if (parameters == null || parameters.isEmpty()){
            return Collections.emptyMap();
        } else {
            try {
                Class<?> cl =Class.forName("com.branegy.dbmaster.gwt.module.tools.model.PrimitiveMapDecoder");
                Method method = cl.getMethod("decode", String.class);
                method.setAccessible(true);
                return (Map<String, Object>) method.invoke(null, parameters);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
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
    public String getParentToolId() {
        return parentToolId;
    }
    public void setParentToolId(String parentToolId) {
        this.parentToolId = parentToolId;
    }
}
