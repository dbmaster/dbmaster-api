package com.branegy.dbmaster.connection;

import com.branegy.service.connection.model.DatabaseConnection;

/**
 * Represents a way to reach a resource.
 * E.g. connection to sql server can be reached via jdbc driver or odbc connection.
 */
abstract class Connector {
    protected final DriverInfo driverInfo;
    protected final DatabaseConnection databaseConnection;

    protected Connector(DriverInfo driverInfo, DatabaseConnection databaseConnection){
        this.databaseConnection = databaseConnection;
        this.driverInfo = driverInfo;
    }
    
    public DatabaseConnection getConnectionInfo() {
        return databaseConnection;
    }

    public abstract Dialect connect();
    
}