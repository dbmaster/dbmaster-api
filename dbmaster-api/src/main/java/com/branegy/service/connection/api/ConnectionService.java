package com.branegy.service.connection.api;

import java.util.List;

import com.branegy.dbmaster.connection.ConnectorInfo;
import com.branegy.dbmaster.model.DatabaseInfo;
import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface ConnectionService {

    DatabaseConnection findConnectionById(long id);
    /**
     * @param <project-name>.<connection-name> || <connection-name>
     * @return
     */
    DatabaseConnection findByName(String connectionName);
    
    List<DatabaseInfo> testConnection(DatabaseConnection connection);
    List<ConnectorInfo> getDriverList();

    DatabaseConnection createConnection(DatabaseConnection connection);
    DatabaseConnection updateConnection(DatabaseConnection connection);
    void deleteConnection(DatabaseConnection connection);

    List<DatabaseConnection> getConnectionList();
  
    
    // TODO (Slava) review this function below
    /**
     * the some of getConnectionSlice, but with search over all project
     * DO NOT USE IT. gwt only api
     */
    Slice<DatabaseConnection> getFullConnectionList(int offset,int limit, String query);

    
    // TODO (Slava) rename to getConnectionList; get rid of name parameter
    /**
     * 
     * @param params
     * @param name
     * @return
     */
    Slice<DatabaseConnection> getConnectionSlice(QueryRequest params, String name);
    String getConnectionExtraInfo(DatabaseConnection connection);
}
