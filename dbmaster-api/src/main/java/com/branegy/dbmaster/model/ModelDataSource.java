package com.branegy.dbmaster.model;

import static com.branegy.dbmaster.model.ModelDataSource.QUERY_MODELDATASOURCE_BY_PROJECT;
import static com.branegy.dbmaster.model.ModelDataSource.QUERY_MODELDATASOURCE_BY_PROJECT_MODEL_DATASOURCE;
import static com.branegy.dbmaster.model.ModelDataSource.QUERY_MODELDATASOURCE_COUNT_BY_PROJECT;
import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;
import com.branegy.service.connection.model.DatabaseConnection;

@Entity
@javax.persistence.Table(name = "db_model_datasource")
@CustomFieldDiscriminator(ModelDataSource.CUSTOM_FIELD_DISCRIMINATOR)
@NamedQueries({
    @NamedQuery(name = QUERY_MODELDATASOURCE_BY_PROJECT,
            query = "from ModelDataSource m where m.model.project.id=:projectId order by m.order asc, m.name asc"),
    @NamedQuery(name = QUERY_MODELDATASOURCE_COUNT_BY_PROJECT,
            query = "select count(m) from ModelDataSource m where m.model.id=:projectId"),
    
    @NamedQuery(name = QUERY_MODELDATASOURCE_BY_PROJECT_MODEL_DATASOURCE,
            query = "from ModelDataSource ds "
                  + "where ds.model.project.id=:projectId and ds.name=:dataSource and ds.model.name=:model"),
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from db_model_datasource ds " +
                              "inner join  DB_MODEL m on ds.model_id = m.id " +
                              "where m.project_id=:projectId")
public class ModelDataSource extends DatabaseObject<ModelDataSource> {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "ModelDatasource";
    
    public static final String QUERY_MODELDATASOURCE_BY_PROJECT = "ModelDataSource.findByProjectId";
    public static final String QUERY_MODELDATASOURCE_BY_PROJECT_MODEL_DATASOURCE = 
                                                            "ModelDataSource.findByProjectModelDataSource";
    public static final String QUERY_MODELDATASOURCE_COUNT_BY_PROJECT = "ModelDataSource.findCountByProjectId";
    
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
    @JoinColumn(name="model_id")
    Model model;

    @Column(name="name",length=255)
    @Size(min=1,max=255)
    @NotNull
    String name;
    
    @Column(name="readonly_flag",nullable = false) // TODO
    boolean readonly;
    
    @Column(name="order_index",nullable = false)
    @NotNull
    byte order;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="connection_id")
    DatabaseConnection connection;

    @Embedded
    RevEngineeringOptions options;

    @OneToMany(mappedBy = "datasource", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '0Tabl'")
    List<Table> tables;

    @OneToMany(mappedBy = "datasource", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '1View'")
    List<View> views;

    @OneToMany(mappedBy = "datasource", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '2Proc'")
    List<Procedure> procedures;
    
    @OneToMany(mappedBy = "datasource", targetEntity = ModelObject.class,
            orphanRemoval = true, cascade={PERSIST, REMOVE, REFRESH, DETACH})
    @Where(clause = "DTYPE = '3Func'")
    List<Function> functions;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastSynch",nullable = false)
    @NotNull
    Date lastSynch;
    
    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
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
    final void setParent(ModelDataSource parent) {
    }

    @Override
    final ModelDataSource getParent() {
        return null;
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

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public byte getOrder() {
        return order;
    }

    public void setOrder(byte order) {
        this.order = order;
    }
}
