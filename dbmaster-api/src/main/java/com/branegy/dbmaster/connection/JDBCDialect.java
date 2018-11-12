package com.branegy.dbmaster.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branegy.dbmaster.model.Column;
import com.branegy.dbmaster.model.DatabaseInfo;
import com.branegy.dbmaster.model.Function;
import com.branegy.dbmaster.model.Model;
import com.branegy.dbmaster.model.Procedure;
import com.branegy.dbmaster.model.RevEngineeringOptions;
import com.branegy.dbmaster.model.Table;
import com.branegy.dbmaster.model.View;
import com.branegy.dbmaster.util.NameMap;
import com.branegy.inventory.model.Job;
import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.core.exception.ApiException;

public abstract class JDBCDialect implements Dialect {
    protected static final Logger logger = LoggerFactory.getLogger(JDBCDialect.class);

    protected final JdbcConnector connector;
    protected final boolean catalog;
    
    protected Set<String> typesWithSize;
    protected Set<String> typesWithScale;
    protected Set<String> typesWithPrecesion;
    
    private boolean caseSensitive;
    
    public JDBCDialect(JdbcConnector cp, Connection connection) throws SQLException {
        this(true, cp, connection);
    }
    
    /**
     * @param catalog indicates that dialect will use catalogs terminology instead of schemas
     * @param cp
     * @throws SQLException
     */
    public JDBCDialect(boolean catalog, JdbcConnector cp, Connection connection) throws SQLException {
        this.catalog = catalog;
        this.connector = cp;
        this.caseSensitive = populateCaseSensitivity(connection);
    }

    protected boolean populateCaseSensitivity(Connection connection) throws SQLException {
        return connection.getMetaData().supportsMixedCaseIdentifiers();
    }

    protected NameMap<Table> getTables(RevEngineeringOptions options) {
        try {
            Connection conn = getProvider().getJdbcConnection(options.database);

            NameMap<Table> tables = new NameMap<Table>(200);

            DatabaseMetaData metaData = conn.getMetaData();

            boolean system = false;

            ResultSet tablesRs = metaData.getTables(catalog? fixFilter(options.database): null,
                    catalog? null: fixFilter(options.database),
                    fixFilter(options.includeTables), null);

            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME");
                String type = tablesRs.getString("TABLE_TYPE");
                if (type.equals("TABLE") || system) {
                    Table tableInfo = new Table();
                    tableInfo.setName(tableName);
                    // remarks for ORACLE is null
                    String comments = tablesRs.getString("REMARKS");
                    tableInfo.setCustomData(Column.DESCRIPTION, comments);
                    tables.put(tableName, tableInfo);
                }
                // populateIndexes(c,metaData, tableInfo);
            }
            tablesRs.close();
            populateTableColumns(conn, options, tables);
            conn.close();

            return tables;
        } catch (Exception e) {
            throw new ConnectionApiException("Cannot load tables ", e);
        }
    }
    
    protected JdbcConnector getProvider() {
        return connector;
    }

    protected void populateTableColumns(Connection conn, RevEngineeringOptions options,
            NameMap<Table> tables) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        for (Table table : tables.values()) {
            ResultSet columnsRS = null;
            try {

                String tableName = table.getName();
                columnsRS = metaData.getColumns(catalog?options.database:null,
                        catalog?null:options.database,
                        tableName, null);

                while (columnsRS.next()) {

                    Column column = new Column();
                    column.setName(columnsRS.getString("COLUMN_NAME"));
                    // int type = columns.getInt("DATA_TYPE"); // int => SQL type from
                    // java.sql.Types
                    String type = columnsRS.getString("TYPE_NAME");
                    column.setType(type);
                    if (typesWithSize.contains(type.toUpperCase())) {
                        column.setSize(columnsRS.getInt("COLUMN_SIZE"));
                    }

                    if (typesWithScale.contains(type.toUpperCase())) {
                        column.setPrecesion(column.getSize());
                        column.setScale(columnsRS.getInt("DECIMAL_DIGITS"));
                    }

                    String nullable = columnsRS.getString("NULLABLE");
                    column.setNullable(nullable != null && nullable.equals("1"));

                    String string = columnsRS.getString("COLUMN_DEF");
                    column.setDefaultValue(string);

                    // boolean unsigned = typeName.endsWith(" unsigned");
                    table.addColumn(column);
                }
            } catch (SQLException e) {
                logger.error("",e);
            } finally {
                columnsRS.close();
            }
        }

        setTableColumnComments(conn, options.database, tables);
    }

    protected void populateViewColumns(Connection conn, RevEngineeringOptions options,
            NameMap<View> views) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        for (View view : views.values()) {
            ResultSet columnsRS = null;
            try {

                String viewName = view.getSimpleName();
                columnsRS = metaData.getColumns(options.database, view.getSchema(), viewName, null);

                while (columnsRS.next()) {

                    Column column = new Column();
                    column.setName(columnsRS.getString("COLUMN_NAME"));
                    // int type = columns.getInt("DATA_TYPE"); // int => SQL type from
                    // java.sql.Types
                    String type = columnsRS.getString("TYPE_NAME");
                    column.setType(type);
                    if (typesWithSize.contains(type.toUpperCase())) {
                        column.setSize(columnsRS.getInt("COLUMN_SIZE"));
                    }

                    if (typesWithScale.contains(type.toUpperCase())) {
                        column.setPrecesion(column.getSize());
                        column.setScale(columnsRS.getInt("DECIMAL_DIGITS"));
                    }

                    String nullable = columnsRS.getString("NULLABLE");
                    column.setNullable(nullable != null && nullable.equals("1"));

                    String string = columnsRS.getString("COLUMN_DEF");
                    column.setDefaultValue(string);

                    // boolean unsigned = typeName.endsWith(" unsigned");
                    view.addColumn(column);
                }
            } catch (SQLException e) {
                logger.error("",e);
            } finally {
                columnsRS.close();
            }
        }

        setViewColumnComments(conn, options.database, views);
    }



    /**
     * @deprecated use getFilter instead
     */
    @Deprecated
    protected String fixFilter(String filter) {
        return filter == null || filter.trim().length() == 0 ? null : filter.replace('*', '%');
    }

    protected String getFilter(String schemaColumn,String objectColumn,
            String includeFilter,String excludeFilter) {
        String inFilter = getFilter(schemaColumn,objectColumn,includeFilter);
        String exFilter = getFilter(schemaColumn,objectColumn,excludeFilter);
        if (inFilter!=null && exFilter!=null) {
            return inFilter +" and not ("+exFilter+")";
        } else if (inFilter!=null) {
            return inFilter;
        } else if (exFilter!=null) {
            return "not ("+exFilter+")";
        } else {
            return null;
        }
    }

    private String getFilter(String schemaColumn,String objectColumn, String filter) {
        if (filter == null || filter.trim().length() == 0) {
            return null;
        }
        StringBuffer sb=new StringBuffer();
        filter = filter.replace('*', '%');
        for (String filterSingle : filter.split(";")) {
            if (sb.length()>0) {
                sb.append(" and ");
            }
            String[] filterParams = filterSingle.split("\\.");
            sb.append('(');
            if (filterParams.length==2) {
                sb.append(schemaColumn).append(" like '").append(filterParams[0]).append("' and ");
                sb.append(objectColumn).append(" like '").append(filterParams[1]).append("')");
            } else {
                sb.append(objectColumn).append(" like '").append(filterParams[0]).append("')");
            }
        }
        return sb.toString();
    }


    protected abstract void setTableColumnComments(Connection conn, String database,
            NameMap<Table> tables) throws SQLException;

    protected abstract void setViewColumnComments(Connection conn, String database,
            NameMap<View> tables) throws SQLException;

    protected DatabaseConnection getCI() {
        return connector.getConnectionInfo();
    }

    public List<DatabaseInfo> getDatabases() {
        Connection conn = null;
        try {
            conn = connector.getJdbcConnection(null);
            List<DatabaseInfo> result = new ArrayList<DatabaseInfo>();
            ResultSet rs = null;
            try{
                if (catalog){
                    rs = conn.getMetaData().getCatalogs();
                    while (rs.next()) {
                        result.add(new DatabaseInfo(rs.getString("TABLE_CAT"),null, true));
                    }
                } else {
                    rs = conn.getMetaData().getSchemas();
                    while (rs.next()) {
                        result.add(new DatabaseInfo(rs.getString("TABLE_SCHEM"),null, true));
                    }
                }
                for (DatabaseInfo dbInfo : result) {
                    dbInfo.setCustomData("State", "ONLINE");
                }
            } finally{
                if (rs!=null){
                    rs.close();
                }
            }
            return result;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionApiException("Cannot load databases:"+e.getLocalizedMessage(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Cannot load databases", e);
            }
        }
    }

    public List<Job> getJobs() {
        Connection conn = null;
        String sql =
        " select job.job_id, job.name, job.enabled, job.description,"
        +      " c.name as category_name, suser_sname(job.owner_sid) as owner "+
        " from msdb.dbo.sysjobs job " +
        " left join msdb.dbo.syscategories c on job.category_id=c.category_id";
        try {
            conn = connector.getJdbcConnection(null);
            java.sql.Statement st;
            List<Job> result = new ArrayList<Job>();
            ResultSet rs = null;
            try{
                st = conn.createStatement();
                rs = st.executeQuery(sql);
                while (rs.next()) {
                    Job job = new Job();
                    job.setJobType("SqlServerJob");
                    job.setJobName(rs.getString("name"));

                    job.setCustomData("JobId", rs.getString("job_id"));
                    job.setCustomData("Enabled", rs.getBoolean("enabled"));
                    job.setCustomData("Source", "SqlServer");
                    job.setCustomData("Description", rs.getString("description"));
                    job.setCustomData("Category", rs.getString("category_name"));
                    job.setCustomData("Owner", rs.getString("owner"));
                    job.setCustomData("ExtraInfo", "TODO");
                    
                    result.add(job);
                }
            } finally{
                if (rs!=null){
                    rs.close();
                }
            }
            return result;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionApiException("Cannot load jobs:"+e.getLocalizedMessage(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Cannot load jobs", e);
            }
        }
    }

    public Model getModel(String name, RevEngineeringOptions options) {
        Model model = new Model();
        model.setConnection(connector.getConnectionInfo());
        model.setName(name);
        model.setOptions(options);
        model.setLastSynch(new Date());
        model.setCustomData("dialect", getDialectName());
        model.setCustomData("dialect_version", getDialectVersion());

        if (options.importTables) {
            model.setTables(getTables(options).toList());
        }
        if (options.importViews) {
            model.setViews(getViews(options).toList());
        }
        if (options.importProcedures) {
            model.setProcedures(getProcedures(options).toList());
            model.setFunctions(getFunctions(options).toList());
        }
        return model;
    }

    protected NameMap<View> getViews(RevEngineeringOptions options) {
        return new NameMap<View>(0);
    }

    protected NameMap<Procedure> getProcedures(RevEngineeringOptions options) {
        return new NameMap<Procedure>(0);
    }
    
    protected NameMap<Function> getFunctions(RevEngineeringOptions options) {
        return new NameMap<Function>(0);
    }
    
    @Override
    public void close() {
        // TODO Dialect should be like a connection
        // Need replacing existing code
    }

    public final String getDialectName() {
        if (connector.dialectName==null){
            throw new NullPointerException();
        }
        return connector.dialectName;
    }
    
    public final String getDialectVersion() {
        if (connector.dialectVersion==null) {
            throw new NullPointerException();
        }
        return connector.dialectVersion;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

}
