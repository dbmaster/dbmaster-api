package com.branegy.dbmaster.connection;

import java.sql.Connection;
import java.util.List;

import com.branegy.dbmaster.model.DatabaseInfo;
import com.branegy.dbmaster.model.ModelDataSource;
import com.branegy.dbmaster.model.RevEngineeringOptions;
import com.branegy.inventory.model.Job;

/**
 * This interface represents a JDBC connection to resource.
 */
public interface JdbcDialect extends Dialect{
    Connection getConnection();

    List<DatabaseInfo> getDatabases();
    ModelDataSource getModelDataSource(RevEngineeringOptions options);
    List<Job> getJobs();
    String getDialectName();
    String getDialectVersion();
    boolean isCaseSensitive();
}