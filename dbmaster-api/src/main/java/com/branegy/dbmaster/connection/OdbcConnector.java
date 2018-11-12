package com.branegy.dbmaster.connection;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.core.exception.ApiException;
import com.branegy.util.DataDirHelper;

public class OdbcConnector extends JdbcConnector {
    public OdbcConnector(ConnectorInfo driverInfo, DatabaseConnection ci) {
        super(driverInfo, ci);
    }

   protected JDBCDialect loadDialect() {
        Connection connection = null;
        try {
            connection = getJdbcConnection(null);
            DatabaseMetaData dbmd = connection.getMetaData();

            logger.info("=====  Database info =====");
            logger.info("DatabaseProductName: " + dbmd.getDatabaseProductName() );
            logger.info("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion() );
  
            String databaseVersion;
            try{
                databaseVersion = ""+dbmd.getDatabaseMajorVersion();
            } catch (Exception e){
                databaseVersion = dbmd.getDatabaseProductVersion();
                if (databaseVersion.indexOf('.')>0){
                    databaseVersion = databaseVersion.substring(0, databaseVersion.indexOf('.'));
                }
            }
            dialectName = dbmd.getDatabaseProductName();
            String dialectFileName = dialectName+ "-"+databaseVersion;
            
            File scriptFile = new File(DataDirHelper.getDataDir()+"dialects/"+ dialectFileName +".groovy");
            if (!scriptFile.exists()){
                scriptFile = new File(scriptFile.getParent(),"jdbc-default.groovy");
            }
            return instanceDialect(scriptFile, connection);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionApiException(e.getMessage(), e);
        } finally {
            closeConnection(connection);
        }
    }

    
}