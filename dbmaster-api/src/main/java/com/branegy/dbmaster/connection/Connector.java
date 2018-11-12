package com.branegy.dbmaster.connection;

import com.branegy.service.connection.model.DatabaseConnection;

/**
 * Represents a way to reach a resource.
 * E.g. connection to sql server can be reached via jdbc driver or odbc connection.
 */
public abstract class Connector {
    protected final ConnectorInfo driverInfo;
    protected final DatabaseConnection ci;

    protected Connector(ConnectorInfo driverInfo, DatabaseConnection ci){
        this.ci = ci;
        this.driverInfo = driverInfo;
    }
    
    public DatabaseConnection getConnectionInfo() {
        return ci;
    }

    public abstract Dialect connect();
    
     /** should be replaced with getDialect
                (when dialect will open connection) */
     @Deprecated
    public abstract boolean testConnection();
     
}