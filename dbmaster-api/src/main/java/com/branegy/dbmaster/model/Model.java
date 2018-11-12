package com.branegy.dbmaster.model;

import static com.branegy.dbmaster.model.Model.QUERY_MODEL_BY_PROJECT;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_BY_PROJECT_NAME;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_BY_PROJECT_NAME_VERSION;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_COUNT_BY_PROJECT;
import static com.branegy.dbmaster.model.Model.QUERY_MODEL_COUNT_BY_PROJECT_NAME;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;
import com.branegy.service.connection.model.DatabaseConnection;

@Entity
@javax.persistence.Table(name = "db_model", uniqueConstraints={
    @UniqueConstraint(columnNames = {"project_id","name","version"}),
    @UniqueConstraint(columnNames = {"project_id","name","lastSynch"})
})
@CustomFieldDiscriminator("Model")
@NamedQueries({
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT,
            query = "from Model m where m.project.id=:projectId order by m.name asc, m.lastSynch asc"),
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT_NAME,
            query = "from Model m where m.project.id=:projectId and m.name=:name order by lastSynch desc"),
    @NamedQuery(name = QUERY_MODEL_BY_PROJECT_NAME_VERSION,
        query = "from Model m where m.project.id=:projectId and m.name=:name and m.version=:version"),
    @NamedQuery(name = QUERY_MODEL_COUNT_BY_PROJECT,
            query = "select count(m) from Model m where m.project.id=:projectId"),
    @NamedQuery(name = QUERY_MODEL_COUNT_BY_PROJECT_NAME,
            query = "select count(m) from Model m where m.project.id=:projectId and m.name<:name")
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from db_model where project_id=:projectId")
public class Model extends DatabaseObject<Model> {
    public static final String QUERY_MODEL_BY_PROJECT = "Model.findByProjectId";
    public static final String QUERY_MODEL_BY_PROJECT_NAME = "Model.findByProjectName";
    public static final String QUERY_MODEL_BY_PROJECT_NAME_VERSION = "Model.findByProjectNameVersion";
    public static final String QUERY_MODEL_COUNT_BY_PROJECT = "Model.findCountByProjectId";
    public static final String QUERY_MODEL_COUNT_BY_PROJECT_NAME = "Model.findCountByProjectIdName";
    
    public static final String FETCH_TABLES_VIEWS_PROCEDURES_FUNCTIONS = "tables,views,procedures,functions";
    public static final String FETCH_TREE =
        "tables.columns,tables.parameters,tables.constraints,tables.foreignKeys,tables.indexes,"+
        "views.columns,views.parameters,views.constraints,views.foreignKeys,views.indexes,"+
        "procedures.columns,procedures.parameters,procedures.constraints," +
                    "procedures.foreignKeys,procedures.indexes,"+
        "functions.columns,functions.parameters,functions.constraints," +
                    "functions.foreignKeys,functions.indexes";
    
    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;

    @Column(name="name",length=255, nullable = false)
    @Size(min=1,max=255)
    @NotNull
    String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="connection_id")
    DatabaseConnection connection;

    @Embedded
    RevEngineeringOptions options;

    @OneToMany(mappedBy = "model", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '0Tabl'")
    List<Table> tables;

    @OneToMany(mappedBy = "model", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '1View'")
    List<View> views;

    @OneToMany(mappedBy = "model", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '2Proc'")
    List<Procedure> procedures;
    
    @OneToMany(mappedBy = "model", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '3Func'")
    List<Function> functions;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastSynch",nullable = false)
    @NotNull
    Date lastSynch;
    
    @Column(name="version",nullable = false, length=255)
    @Size(min=1,max=255)
    @NotNull
    String version;
    
    @Formula("lastSynch = (select max(m.lastSynch) from db_model m where m.name = name " +
            "and m.project_id = project_id)")
    boolean actualVersion;
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseConnection getConnection() {
        return connection;
    }

    public void setConnection(DatabaseConnection connection) {
        this.connection = connection;
    }

    public RevEngineeringOptions getOptions() {
        return options;
    }

    public void setOptions(RevEngineeringOptions options) {
        this.options = options;
    }

    public Date getLastSynch() {
        return lastSynch;
    }

    public void setLastSynch(Date lastSynch) {
        this.lastSynch = lastSynch;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isActualVersion() {
        return actualVersion;
    }
    
    public String getConnectionName(){
        return connection==null?null:connection.getProject().getName()+"."+connection.getName();
    }

    public List<Table> getTables() {
        return unmodifiableList(tables);
    }

    public void setTables(List<Table> tables) {
        this.tables = mergeList(this.tables, tables);
    }

    public List<View> getViews() {
        return unmodifiableList(views);
    }

    public void setViews(List<View> views) {
        this.views = mergeList(this.views, views);
    }

    public List<Procedure> getProcedures() {
        return unmodifiableList(procedures);
    }

    public void setProcedures(List<Procedure> procedures) {
        this.procedures = mergeList(this.procedures, procedures);
    }
    
    public List<Function> getFunctions() {
        return unmodifiableList(functions);
    }

    public void setFunctions(List<Function> functions) {
        this.functions = mergeList(this.functions, functions);
    }

    public Table getTable(String tableName) {
        return findByName(tables, tableName, "tableName");
    }

    public View getView(String viewName) {
        return findByName(views, viewName, "viewName");
    }

    public Procedure getProcedure(String procedureName) {
        return findByName(procedures, procedureName, "procedureName");
    }
    
    public Function getFunction(String functionName) {
        return findByName(functions, functionName, "functionName");
    }
    
    public void addTable(Table table){
        tables = addChild(tables, table, "table");
    }
    
    public void addView(View view){
        views = addChild(views, view, "view");
    }
    
    public void addProcedure(Procedure procedure){
        procedures = addChild(procedures, procedure, "procedure");
    }
    
    public void addFunction(Function function){
        functions = addChild(functions, function, "function");
    }
    
    public void removeTable(Table table){
        removeChild(tables, table,"table");
    }
    
    public void removeView(View view){
        removeChild(views, view, "view");
    }
    
    public void removeProcedure(Procedure procedure){
        removeChild(procedures, procedure, "procedure");
    }
    
    public void removeFunction(Function function){
        removeChild(functions, function, "function");
    }
    
    @Override
    final void setParent(Model parent) {
    }

    @Override
    final Model getParent() {
        return null;
    }
}
