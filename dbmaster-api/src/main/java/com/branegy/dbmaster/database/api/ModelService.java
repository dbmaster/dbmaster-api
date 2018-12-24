package com.branegy.dbmaster.database.api;

import java.util.List;
import java.util.Map;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.model.Column;
import com.branegy.dbmaster.model.DatabaseObject;
import com.branegy.dbmaster.model.Model;
import com.branegy.dbmaster.model.ModelObject;
import com.branegy.dbmaster.model.RevEngineeringOptions;
import com.branegy.dbmaster.sync.api.SyncSession;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface ModelService {

    /**
     * Searches for column by id
     */
    Column findColumnById(long id);

    /**
     * Creates or updates a column
     * @param fetchPath
     */
    Column saveColumn(Column column, String fetchPath);

    /**
     * Removes column by id
     */
    void deleteColumn(long columnId);

    /**
     * Removes parameter of a stored procedure by id
     */
    void deleteParameter(long parameterId);

    
    <T extends ModelObject> T findModelObjectByName(long modelId, String modelObjectName, Class<T> clazz);
    
    Slice<Column> getColumnListByModel(String modelName, String modelVersion,  QueryRequest request);

    Slice<Column> getColumnListByModel(String modelName, String modelVersion, String name,
            Class<? extends ModelObject> clazz, int limit, String fetchPath);

    Slice<Column> getColumnListByModelObject(String modelName, String modelVersion,
            String modelObjectName, Class<? extends ModelObject> clazz, QueryRequest request);
    
    Slice<Column> getColumnListByModelObject(String modelName, String modelVersion,
            String modelObjectName, Class<? extends ModelObject> clazz, String columnName,
            int limit, String fetchPath);
    
    Slice<Column> getColumnList(Long modelObjectId, Long modelId, QueryRequest request);

    /**
     * Creates a model.
     * @param model
     * @param fetchPath - names of relationships to pull separated by comma (TODO confirm)
     *        we need to have project name to build full reference to the connection
     *        during model conversion to UI bean while model.connection.project uses lazy loading
     * @return
     */
    Model createModel(Model model, String fetchPath);

    Model saveModel(Model model,String fetchPath);

    void deleteModel(long id);

    Model findModelById(long id);
    Model findModelById(long id, String fetchPath);

    // TODO fetchpath format should be described
    @Deprecated
    /**
     * use findModelByProjectName(Project project, null, String version,String fetchPath);
     */
    Model findModelByProjectName(Project project, String name, String fetchPath);
    Model findModelByProjectName(Project project, String name, String version, String fetchPath);
    
    /**
     * use findModelByName(name, null, fetchPath)
     */
    @Deprecated
    Model findModelByName(String name, String fetchPath);
    Model findModelByName(String name, String version, String fetchPath);

    Slice<Model> getModelList(Project project, int offset, int limit, String fetchPath);
    
    Slice<Model> getModelList(String query, int offset, int limit, String fetchPath);

    List<Model> getModelList(Project project, String fetchPath);

    ModelObject createModelObject(ModelObject model);

    ModelObject saveModelObject(ModelObject model);

    ModelObject findModelObjectById(long id, String fetchPath);

    void deleteModelObject(long id);

    Slice<ModelObject> getModelObjectList(String modelName, String modelVersion, QueryRequest request);
    
    Slice<ModelObject> getModelObjectList(String modelName, String modelVersion, String modelObjectName,
            Class<? extends ModelObject> clazz, int limit, String fetchPath);

    Model fetchModel(String connectionName, RevEngineeringOptions options);
    
    @Deprecated
    /**
     * use fetchModel instead
     */
    Model importModel(String connectionName, RevEngineeringOptions options);
    
    SyncSession synchronizeModel(long modelId);
    
    SyncSession compareModel(Model sourceModel, Model targetModel);

    SyncSession compareObjects(DatabaseObject<?> source, DatabaseObject<?> target);
    SyncSession compareObjects(DatabaseObject<?> source, DatabaseObject<?> target, Map<String,Object> params);
}