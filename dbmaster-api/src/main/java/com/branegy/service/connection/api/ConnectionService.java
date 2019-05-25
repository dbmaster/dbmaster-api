package com.branegy.service.connection.api;

import java.util.List;

import com.branegy.dbmaster.connection.DriverInfo;
import com.branegy.dbmaster.model.DatabaseInfo;
import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;
import com.branegy.service.core.exception.EntityNotFoundApiException;

public interface ConnectionService {

    DatabaseConnection findConnectionById(long id);
    /**
     * @param connectionName should be in &lt;project-name&gt;.&lt;connection-name&gt; || &lt;connection-name&gt;
     * @return database connection 
     */
    DatabaseConnection findByName(String connectionName) throws EntityNotFoundApiException;
    
    List<DatabaseInfo> testConnection(DatabaseConnection connection);
    List<DriverInfo> getDriverList();

    DatabaseConnection createConnection(DatabaseConnection connection);
    DatabaseConnection updateConnection(DatabaseConnection connection);
    void deleteConnection(DatabaseConnection connection);

    List<DatabaseConnection> getConnectionList();
    Slice<DatabaseConnection> getConnectionSlice(QueryRequest params);
  
    String getConnectionExtraInfo(DatabaseConnection connection);
    String getConnectionHistoryInfo(DatabaseConnection connection);
}