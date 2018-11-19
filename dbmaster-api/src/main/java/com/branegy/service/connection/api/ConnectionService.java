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
     * @param connectionName should be in &lt;project-name&gt;.&lt;connection-name&gt; || &lt;connection-name&gt;
     * @return database connection 
     */
    // TODO what is return when connection is not found
    DatabaseConnection findByName(String connectionName);
    
    List<DatabaseInfo> testConnection(DatabaseConnection connection);
    List<ConnectorInfo> getDriverList();

    DatabaseConnection createConnection(DatabaseConnection connection);
    DatabaseConnection updateConnection(DatabaseConnection connection);
    void deleteConnection(DatabaseConnection connection);

    List<DatabaseConnection> getConnectionList();
  
    
    // TODO (Slava) review this function below
    // TODO Replace with QueryRequest
    /**
     * @param offset defines number of objects to skip
     * @param limit total number of objects to return 
     * @param query search string and filter some connections out
     * @return all connections ignoring current project context
     */
    Slice<DatabaseConnection> getFullConnectionList(int offset, int limit, String query);

    
    // TODO (Slava) rename to getConnectionList; get rid of name parameter
    Slice<DatabaseConnection> getConnectionSlice(QueryRequest params, String name);

    String getConnectionExtraInfo(DatabaseConnection connection);
    String getConnectionHistoryInfo(DatabaseConnection connection);
}