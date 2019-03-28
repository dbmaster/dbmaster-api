package com.branegy.inventory.api;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import com.branegy.dbmaster.sync.api.SyncSession;
import com.branegy.inventory.model.Application;
import com.branegy.inventory.model.ApplicationLink;
import com.branegy.inventory.model.Database;
import com.branegy.inventory.model.DatabaseUsage;
import com.branegy.inventory.model.Installation;
import com.branegy.inventory.model.Job;
import com.branegy.inventory.model.SecurityObject;
import com.branegy.inventory.model.SecurityObjectLink;
import com.branegy.inventory.model.Server;
import com.branegy.persistence.BaseEntity;
import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

// TODO (Slava) Review save or update naming convention
// TODO (Slava) replace findObject with getObject
public interface InventoryService {

    SyncSession getDatabaseListDiff(Logger logger);
    SyncSession getDatabaseListDiff(Logger logger, Collection<DatabaseConnection> connections);

    // ---- applications
    Slice<Application> getApplicationList(QueryRequest request);
    
    /**
     * Used in url based initial load.
     * @param applicationName name of the application specified in url
     * @param limit page limit
     * @return page where applicationName is located.
     */
    Slice<Application> getApplicationList(String applicationName, int limit);
    
    String getApplicationExtraInfo(Application application);
    Application findApplicationById(long applicationId);
    Application findApplicationByName(String applicationName);

    Application createApplication(Application application);
    Application updateApplication(Application application);
    void deleteApplication(long applicationId);


    // ---- databases
    Database findDatabaseById(long id);
    Database findDatabaseByConnectionNameDbName(String connectionName, String dbName);
    @Deprecated
    Database findDatabaseByServerNameDbName(String serverName, String dbName);
    String getDatabaseExtraInfo(Database database);
    String getDatabaseHistoryInfo(Database database);
    
    /**
     * @return list of all databases in the current project excluding deleted ones
     */
    List<Database> getDatabaseList();
    
    /**
     * @param request holds search criteria, desired sort order, paging, and return attributes
     * @return Deleted=false only for default
     */
    Slice<Database> getDatabaseList(QueryRequest request);
    Slice<Database> getDatabaseList(int limit,String serverName,String dbName);

    List<Database> findDatabaseByServer(String databaseServer);
    Database createDatabase(Database database);
    Database updateDatabase(Database database);
    void deleteDatabase(long id);


    DatabaseUsage createDbUsage(long databaseId, long applicationId, String role);
    DatabaseUsage createDbUsage(DatabaseUsage dbUsage);
    DatabaseUsage updateDbUsage(DatabaseUsage dbUsage);
    DatabaseUsage findDbUsageById(long id);
    List<DatabaseUsage> findDBUsageByInstanceId(long installationId);
    Slice<DatabaseUsage> findDBUsageByInstanceId(long installationId, int offset, int limit);
    Slice<DatabaseUsage> findDBUsageByConnectionNameDbName(String connectionName, String dbName,
            int offset, int limit);
    @Deprecated
    Slice<DatabaseUsage> findDBUsageByServerNameDbName(String serverName, String dbName,
            int offset, int limit);
    Slice<DatabaseUsage> findDBUsageByApplicationName(String applicationName,int offset, int limit);
    void deleteDbUsage(long id);
    
    /**
     * @return list of all database usages in the current project (excluding deleted databases)
     */
    List<DatabaseUsage> getDBUsageList();
     
    Installation createIntallation(long serverId, long applicationId, String instanceName);
    void deleteApplicationInstance(long id);
    Installation findApplicationInstanceById(long id);
    Installation saveApplicationInstance(Installation appInstance);
    List<Installation> findInstallationByApplication(long applicationId);
    Slice<Installation> findInstallationByApplication(long applicationId,int offset,int limit);
    List<Installation> findInstallationByServer(long serverId);
    Slice<Installation> findInstallationByServer(long serverId,int offset,int limit);
    List<Installation> searchInstallations(String filter);
    Slice<Installation> searchInstallations(String filter,int offset,int limit);
    List<Installation> getInstallationList();

    Server createServer(Server server);
    void deleteServer(long serverId);
    Server findServerById(long serverId);
    Server findServerByName(String serverName);
    Server saveServer(Server server);
    String getServerExtraInfo(Server server);

    Slice<Server> getServerList(QueryRequest request);
    Slice<Server> getServerList(String name, int pageSize);
    List<Server> getServerList(String serverFilter);
    
    // jobs
    Job createJob(Job job);
    void deleteJob(long jobId);
    Job findJobById(long jobId);
    Job findJobByKey(String jobName, String jobType, String serverName);
    Job saveJob(Job job);
    
    Slice<Job> getJobList(QueryRequest request);
    Slice<Job> getJobList(String jobName, String jobType, String serverName, int pageSize);
    @Deprecated // NOT IMPLEMENTED
    List<Server> getJobList(String jobName, String jobType, String serverName);
    String getJobExtraInfo(Job job);
    String getJobHistoryInfo(Job job);
    
    // application link
    ApplicationLink createApplicationLink(ApplicationLink server);
    void deleteApplicationLink(long applicationLinkId);
    ApplicationLink saveApplicationLink(ApplicationLink applicationLink);
    ApplicationLink findApplicationLinkById(long applicationLinkId);
    
    Slice<ApplicationLink> findApplicationLinkListByObject(BaseEntity obj, QueryRequest request);
    List<ApplicationLink> findApplicationLinkListByObjectClass(Class<? extends BaseEntity> objClass);
    
    
    //SecurityObjectExtraAction
    
    String getSecurityObjectExtraInfo(SecurityObject securityObject);
    String getSecurityObjectHistoryInfo(SecurityObject securityObject);
    
    SecurityObject findSecurityObjectById(long securityObjectId);
    SecurityObject findSecurityObjectByKey(String source, String serverName, String id);
    SecurityObject createSecurityObject(SecurityObject so);
    SecurityObject saveSecurityObject(SecurityObject so);
    void deleteSecurityObject(long id);

    Slice<SecurityObject> getSecurityObjectList(String source, String serverName, String id, int limit);
    Slice<SecurityObject> getSecurityObjectList(QueryRequest request);
    
    SecurityObjectLink findSecurityObjectLinkById(long securityObjectLinkId);
    SecurityObjectLink createSecurityObjectLink(SecurityObjectLink sol);
    SecurityObjectLink updateSecurityObjectLink(SecurityObjectLink sol);
    void deleteSecurityObjectLink(long securityObjectLinkId);
    
    List<SecurityObjectLink> getSecurityObjectLinkList();
    List<SecurityObjectLink> getSecurityObjectLinkList(long securityObjectLinkId, boolean onlyActive);
}