package com.branegy.dbmaster.database.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.custom.CustomFieldConfig;
import com.branegy.dbmaster.model.Column;
import com.branegy.dbmaster.model.DatabaseObject;
import com.branegy.dbmaster.model.Model;
import com.branegy.dbmaster.model.ModelDataSource;
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
    
    Slice<Column> getColumnListByModel(String modelName, String datasourceName, QueryRequest request);

    Slice<Column> getColumnListByModel(String modelName, String datasourceName, String name,
            Class<? extends ModelObject> clazz, int limit, String fetchPath);

    Slice<Column> getColumnListByModelObject(String modelName, String databaseName,
            String modelObjectName, Class<? extends ModelObject> clazz, QueryRequest request);
    
    Slice<Column> getColumnListByModelObject(String modelName, String datasourceName,
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
    ModelDataSource createModelDataSource(ModelDataSource model, String fetchPath);
    

    ModelDataSource saveModelDataSource(ModelDataSource model,String fetchPath);
    
    Model saveModel(Model model,String fetchPath);

    void deleteModel(long id);

    ModelDataSource findModelDataSourceById(long id);
    ModelDataSource findModelDataSourceById(long id, String fetchPath);

    // TODO fetchpath format should be described
    ModelDataSource findModelDataSourceByProjectModelDataSource(Project project, String modelName2, String dataSourceName, String fetchPath);
    ModelDataSource findModelDataSourceByModelDataSource(String modelName, String dataSourceName, String fetchPath);


    Slice<ModelDataSource> getModelDataSourceSlice(Project project, int offset, int limit, String fetchPath);
    
    Slice<ModelDataSource> getModelDataSourceSlice(String query, int offset, int limit, String fetchPath);

    List<ModelDataSource> getModelDataSourceList(Project project, String fetchPath);

    ModelObject createModelObject(ModelObject model);

    ModelObject saveModelObject(ModelObject model);

    ModelObject findModelObjectById(long id, String fetchPath);

    void deleteModelObject(long id);

    Slice<ModelObject> getModelObjectList(String modelName, String dataSourceName, QueryRequest request);
    
    Slice<ModelObject> getModelObjectList(String modelName, String dataSourceName, String modelObjectName,
            Class<? extends ModelObject> clazz, int limit, String fetchPath);

    SyncSession synchronizeModel(long modelId);
    
    SyncSession compareModel(ModelDataSource sourceModel, ModelDataSource targetModel);

    SyncSession compareObjects(DatabaseObject<?> source, DatabaseObject<?> target);
    SyncSession compareObjects(DatabaseObject<?> source, DatabaseObject<?> target, Map<String,Object> params);
    
    Collection<CustomFieldConfig> createExtendedPropertiesConfigs(ModelDataSource model);
    
    
    
    
    Slice<Model> getModelSlice(String filter, String fetchPath);
    Slice<Model> getModelSliceByName(String name, String fetchPath);
    
    
    ModelDataSource fetchModelDataSource(String connectionName, RevEngineeringOptions options);
    List<ModelDataSource> fetchModelDataSource(List<Entry<String,RevEngineeringOptions>> dataSources);
    
    Model findModelById(long id);
    Model findModelById(long id, String fetchPath);
    Model findModelByName(String name);
    Model findModelByName(String name, String fetchPath);
    Model createModel(Model model, String fetchPath);
    
    
    void bindSyncSessionToDataSource(ModelDataSource ds, long syncSessionId);
}