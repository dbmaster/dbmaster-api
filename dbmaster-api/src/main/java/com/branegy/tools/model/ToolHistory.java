package com.branegy.tools.model;

import static com.branegy.tools.model.ToolHistory.DELETE_PREPARED_JOB;
import static com.branegy.tools.model.ToolHistory.FAIL_RUNNING_JOB;
import static com.branegy.tools.model.ToolHistory.PRUNE_PROJECT_TOOLID_UPTODATE;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;
import com.branegy.persistence.BaseEntity;
import com.branegy.tools.api.ExportType;


@Entity
@Table(name="dbm_tool_history")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@NamedQuery(name = FAIL_RUNNING_JOB, 
      query = "update ToolHistory h set "
            + "h.finish = CURRENT_TIMESTAMP, h.status = com.branegy.tools.model.ToolHistory$Status.FAILED "
            + "where "
            + "h.status = com.branegy.tools.model.ToolHistory$Status.RUNNING")
@NamedQuery(name = DELETE_PREPARED_JOB, 
      query = "delete from ToolHistory h "
            + "where "
            + "h.status = com.branegy.tools.model.ToolHistory$Status.PREPARED")
@NamedQuery(name = PRUNE_PROJECT_TOOLID_UPTODATE, 
      query = "from ToolHistory h where "
            + "h.project.id = :projectId and h.finish < :untilDate and "
            + "(h.status='OK' or h.status='FAILED' or h.status='CANCELLED') and "
            + "(:toolId is null or h.toolId=:toolId or h.parentToolId=:toolId)")
public class ToolHistory extends BaseEntity{
    public static final String DELETE_PREPARED_JOB = "ToolHistory.deletePreparedJob";
    public static final String FAIL_RUNNING_JOB = "ToolHistory.failRunningJob";
    public static final String PRUNE_PROJECT_TOOLID_UPTODATE = "ToolHistory.pruneForProjectToolIdUpDate";

    /**
     *  bundle version for processing
     */
    @Column(name="framework_version",length=10,nullable=false,updatable=false)
    String frameworkVersion;
    
    @Column(name="tool_id",length=64,nullable=false,updatable=false)
    String toolId;
    
    @Column(name="parent_tool_id",length=64,nullable=false,updatable=false)
    String parentToolId;
    
    @Column(name="tool_name",length=64,nullable=false,updatable=false)
    String toolName;
    
    @Column(name="tool_version",length=32,nullable=false,updatable=false)
    String toolVersion;
    
    @Column(name="tool",length=64*1024,nullable=false, updatable=false)
    @Lob
    @Convert(converter = ToolConfigAttributeConvertor.class)
    ToolConfig tool;
    
    @Column(name="parameters",length=1024*1024,updatable=false)
    @Convert(converter = ParameterAttributeConverter.class)
    @Lob
    Map<String,Object> parameters;
    
    @Column(name="serializer_class",length=64,nullable=false)
    String dataSerializerClass;
   
    @Enumerated(EnumType.STRING)
    @Column(name="export_type",length=4,nullable=false, updatable=false)
    ExportType exportType;

    @Column(name="html_page_count")
    Integer htmlPageCount;
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="user_id")
    User user;
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;
    
    public enum Status{
        PREPARED, OK, FAILED, RUNNING, CANCELLED;
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name="status",length=9,nullable=false)
    Status status;
   
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="start")
    Date start;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="finish")
    Date finish;
    
    public String getFrameworkVersion() {
        return frameworkVersion;
    }
    public void setFrameworkVersion(String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }
    public String getToolId() {
        return toolId;
    }
    public void setToolId(String toolId) {
        this.toolId = toolId;
    }
    public String getToolVersion() {
        return toolVersion;
    }
    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }
    public ToolConfig getTool() {
        return tool;
    }
    public void setTool(ToolConfig tool) {
        this.tool = tool;
    }
    public Map<String, Object> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    public String getDataSerializerId() {
        return dataSerializerClass;
    }
    public void setDataSerializerId(String dataSerializerId) {
        this.dataSerializerClass = dataSerializerId;
    }
    public ExportType getExportType() {
        return exportType;
    }
    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }
    public Integer getHtmlPageCount() {
        return htmlPageCount;
    }
    public void setHtmlPageCount(Integer htmlPageCount) {
        this.htmlPageCount = htmlPageCount;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public Date getStart() {
        return start;
    }
    public void setStart(Date start) {
        this.start = start;
    }
    public Date getFinish() {
        return finish;
    }
    public void setFinish(Date finish) {
        this.finish = finish;
    }
    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }
    public String getToolName() {
        return toolName;
    }
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    public String getParentToolId() {
        return parentToolId;
    }
    public void setParentToolId(String parentToolId) {
        this.parentToolId = parentToolId;
    }
}
