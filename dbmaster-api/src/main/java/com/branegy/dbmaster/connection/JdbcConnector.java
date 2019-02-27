package com.branegy.dbmaster.connection;

import static com.branegy.service.core.helper.BaseServiceImpl.findCause;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branegy.scripting.ScriptExecutor;
import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.core.exception.ApiException;
import com.branegy.util.DataDirHelper;
import com.branegy.util.IOUtils;

class JdbcConnector extends Connector {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public JdbcConnector(DriverInfo driverInfo, DatabaseConnection databaseConnection) {
        super(driverInfo, databaseConnection);
    }

    public Connection getJdbcConnection(String database) {
        try {
            Properties properties = databaseConnection.asProperties();
            String username = databaseConnection.getUsername();
            if (username!=null && !username.isEmpty()) {
                properties.setProperty("user", username);
            }
            String password = databaseConnection.getPassword();
            if (password!=null && !password.isEmpty()) {
                properties.setProperty("password", password);
            }
            if (database!=null && driverInfo.getDatabaseNameProperty()!=null) {
                properties.setProperty(driverInfo.getDatabaseNameProperty(), database);
            }
            properties.values().removeIf(java.util.Objects::isNull);
	
            Driver jdbcDriver = getJdbcDriver();
            if (!jdbcDriver.acceptsURL(databaseConnection.getUrl())) {
                String msg;
                if (databaseConnection.getName() == null) {
                    msg = "Invalid connection URL. Check URL format and try again.";
                } else {
                    msg = "Invalid connection URL for " + databaseConnection.getName() + ". Check URL format and try again.";
                }
                throw new ConnectionApiException(msg);
            }
            final Connection connection = jdbcDriver.connect(databaseConnection.getUrl(), properties);
            if (connection==null) {
                throw new ConnectionApiException(getExceptionPrefix(databaseConnection) + "Check connection URL and try again");
            }
            if (database!=null && driverInfo.getDatabaseNameProperty()==null) {
                connection.setCatalog(database);
            }
            connection.setAutoCommit(false);
            return connection;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            String msg;
            if (findCause(e, ConnectException.class) != null) {
                msg = getExceptionPrefix(databaseConnection) + "Connection refused";
            } else if (findCause(e, UnknownHostException.class) != null) {
                msg = getExceptionPrefix(databaseConnection) + "Cannot resolve host";
                String host = findCause(e, UnknownHostException.class).getMessage();
                if (host!=null) {
                    msg +=" \""+host+"\"";
                }
            } else {
                if (database!=null) {
                    msg = getExceptionPrefix(databaseConnection)+"Database \""+database+"\". "+e.getMessage();
                } else {
                    msg = getExceptionPrefix(databaseConnection)+e.getMessage();
                }
            }
            throw new DriverConnectionApiException(msg,e);
        }
    }
    
    private String getExceptionPrefix(DatabaseConnection dc){
        if (databaseConnection.getName() == null){
            return "Cannot connect: ";
        } else {
            return "Cannot connect to \""+dc.getName()+"\": ";
        }
    }
    
    public synchronized AbstractJdbcDialect connect() {
        return loadDialect();
    }

    protected final Driver getJdbcDriver() {
        try{
            // URL url = new URL("jar:file:/" + path + "!/");
            // cl = new URLClassLoader(new URL[] { url });
            ClassLoader cl = this.getClass().getClassLoader();
            // return (Driver) cl.loadClass(DRIVER_CLASS).newInstance();
            String jdbcDriverClass = driverInfo.getJdbcDriverClass();
            return (Driver)cl.loadClass(jdbcDriverClass).newInstance();
        } catch (ClassNotFoundException e){
            throw new DriverNotFoundApiException(driverInfo);
        } catch (Exception e) {
            throw new ConnectionApiException("Cannot load connection driver "+driverInfo.getName(), e);
        }
    }

    protected AbstractJdbcDialect loadDialect() {
        Connection connection = null;
        String dialectFileName = null;
        try {
            connection = getJdbcConnection(null);
            DatabaseMetaData dbmd = connection.getMetaData();

            if (logger.isDebugEnabled()) {
                logger.debug("=====  Database info =====");
                logger.debug("DatabaseProductName: " + dbmd.getDatabaseProductName() );
                logger.debug("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion() );
                logger.debug("DatabaseMajorVersion: " + dbmd.getDatabaseMajorVersion() );
                logger.debug("DatabaseMinorVersion: " + dbmd.getDatabaseMinorVersion() );
            }

           
            // TODO Move mapping to configuration or make somehow distributed (e.g. via IOC)
            if (dbmd.getDatabaseProductName().equals("Microsoft SQL Server")) {
                dialectFileName="sqlserver";
            } else if (dbmd.getDatabaseProductName().equals("HSQL Database Engine")) {
                dialectFileName="hsqldb";
            } else if (dbmd.getDatabaseProductName().equals("NuoDB")){
                dialectFileName="nuodb";
            } else {
                dialectFileName = dbmd.getDatabaseProductName();
            }

            String dialectName = dialectFileName;
            String dialectVersion = dbmd.getDatabaseMajorVersion()+"-"+dbmd.getDatabaseMinorVersion();
            dialectFileName = dialectName + "-"+dialectVersion;
            File scriptFile = new File(DataDirHelper.getDataDir()+"dialects/"+ dialectFileName+".groovy");
            if (!scriptFile.exists()) {
                dialectVersion = ""+dbmd.getDatabaseMajorVersion();
                String dialectFileName2 = dialectName + "-"+dialectVersion;
                scriptFile = new File(DataDirHelper.getDataDir()+"dialects/"+ dialectFileName2+".groovy");
            }
            if (!scriptFile.exists()) {
                throw new ApiException("Implementation of dialect for database " +
                                        dialectFileName+" was not found");
            }
            return instanceDialect(scriptFile, connection, dialectName, dialectVersion);
        } catch (ApiException e) {
            IOUtils.closeQuietly(connection);
            throw e;
        } catch (FileNotFoundException e){
            IOUtils.closeQuietly(connection);
            throw new ConnectionApiException("Dialect in not found "+ dialectFileName, e);
        } catch (Exception e) {
            IOUtils.closeQuietly(connection);
            throw new ConnectionApiException(e.getMessage(), e);
        }
    }

    protected AbstractJdbcDialect instanceDialect(File scriptFile, Connection connection, String dialectName, String dialectVersion) throws Exception{
        ClassLoader parentCL = getClass().getClassLoader();
        Class<?> dialectClass = ScriptExecutor.parseClass(parentCL, "", scriptFile);
        
        Constructor<?> constr = dialectClass.getConstructor(JdbcConnector.class, Connection.class,String.class,String.class);
        constr.setAccessible(true);
        return (AbstractJdbcDialect)constr.newInstance(this, connection,dialectName,dialectVersion);
    }
}