package com.branegy.dbmaster.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branegy.dbmaster.model.Column;
import com.branegy.dbmaster.model.DatabaseInfo;
import com.branegy.dbmaster.model.Function;
import com.branegy.dbmaster.model.Model;
import com.branegy.dbmaster.model.Procedure;
import com.branegy.dbmaster.model.RevEngineeringOptions;
import com.branegy.dbmaster.model.RevEngineeringOptions.Filter;
import com.branegy.dbmaster.model.Table;
import com.branegy.dbmaster.model.View;
import com.branegy.dbmaster.util.NameMap;
import com.branegy.persistence.custom.BaseCustomEntity;
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
    
    
    protected static final class PreparedSql{
        private final StringBuilder sql;
        private final List<?> parameters = new ArrayList<>();
        
        public PreparedSql(String sql) {
            this.sql = new StringBuilder(sql);
        }
        
        @SuppressWarnings("unchecked")
        public PreparedSql(String sql,List<?> parameters) {
            this.sql =  new StringBuilder(sql);
            ((List<Object>)this.parameters).addAll(parameters);
        }

        public String getSql() {
            return sql.toString();
        }

        public List<?> getParameters() {
            return parameters;
        }
        
        public PreparedStatement populatePreparedStatement(PreparedStatement ps) throws SQLException {
            return populatePreparedStatement(ps,1);
        }
        
        public PreparedStatement populatePreparedStatement(PreparedStatement ps,int from) throws SQLException {
            for (int i=0, len=parameters.size(); i<len; ++i) {
                ps.setObject(from+i, parameters.get(i));
            }
            return ps;
        }
    }
    
    
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
            String TABLE_TYPE = getObjectTypeByClass(Table.class);
            
            Connection conn = getProvider().getJdbcConnection(options.getDatabase());
            NameMap<Table> tables = new NameMap<Table>(200);
            DatabaseMetaData metaData = conn.getMetaData();

            boolean system = false;
            ResultSet tablesRs = metaData.getTables(
                    catalog? filterToSqlLike(options.getDatabase()): null,
                    catalog? null: filterToSqlLike(options.getDatabase()),
                    null,
                    null);

            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME"); // TODO: check schema name?
                if (!options.accept(TABLE_TYPE, tableName)) {
                    continue;
                }
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
                columnsRS = metaData.getColumns(catalog?options.getDatabase():null,
                        catalog?null:options.getDatabase(),
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

        setTableColumnComments(conn, options.getDatabase(), tables);
    }

    protected void populateViewColumns(Connection conn, RevEngineeringOptions options,
            NameMap<View> views) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        for (View view : views.values()) {
            ResultSet columnsRS = null;
            try {

                String viewName = view.getSimpleName();
                columnsRS = metaData.getColumns(options.getDatabase(), view.getSchema(), viewName, null);

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

        setViewColumnComments(conn, options.getDatabase(), views);
    }

    protected String filterToSqlLike(String filter) {
        return StringUtils.isBlank(filter) 
                ? null
                : filter.replace('*', '%')
                        .replace('?', '_');
    }
    
    protected String getObjectTypeByClass(Class<? extends BaseCustomEntity> objecttClazz) {
        return BaseCustomEntity.getDiscriminator(objecttClazz);
    }
    
    protected PreparedSql getSqlFilter(String schemaColumn,String objectColumn,
            String objectType, RevEngineeringOptions options) {
        if (options.isExcludedObjectType(objectType)) {
            return new PreparedSql("1=0"); // = FALSE : empty result
        } else {
            List<Filter> excludedObjects = options.getExcludedObjects(objectType);
            String filter = "(";
            List<Object> parameters = new ArrayList<>();
            if (!options.isIncludeByDefault(objectType)) {
                List<Filter> includedObjects = options.getIncludedObjects(objectType);
                PreparedSql include = generateSqlFilter(schemaColumn, objectColumn, includedObjects, true);
                filter += '('+ include.getSql()+") and ";
                parameters.addAll(include.getParameters());
            }
            PreparedSql excluded = generateSqlFilter(schemaColumn, objectColumn, excludedObjects, false);
            filter += "not("+excluded.getSql()+"))";
            parameters.addAll(excluded.getParameters());
            return new PreparedSql(filter, parameters);
        }
    }
    
    protected PreparedSql getSqlFilter(String schemaColumn,String objectColumn,
            Class<? extends BaseCustomEntity> objecttClazz, RevEngineeringOptions options) {
        return getSqlFilter(schemaColumn, objectColumn, getObjectTypeByClass(objecttClazz),options);
    }
    
    private void sqlLike(StringBuilder sb, List<Object> parameters, String key, String value) {
        if (isCaseSensitive()) {
            sb.append(key).append(" like ?");
            parameters.add(value);
        } else {
            sb.append("UPPER(").append(key).append(") like ?");
            parameters.add(value.toUpperCase());
        }
    }
    
    private PreparedSql generateSqlFilter(String schemaColumn,String objectColumn, List<Filter> filters, 
            boolean defReturn) {
        if (filters.isEmpty()) {
            return new PreparedSql(defReturn?"1=1":"1=0");
        }
        List<Object> parameters = new ArrayList<>();
        StringBuilder sb=new StringBuilder();
        for (Filter filter:filters) {
            if (sb.length()!=0) {
                sb.append(" or ");
            }
            String pattern = filterToSqlLike(filter.getName());
            String[] patternArray = pattern.split("\\.",2);
            sb.append('(');
            if (patternArray.length==2) {
                sqlLike(sb,parameters,schemaColumn,patternArray[0]);
                sb.append(" and ");
                sqlLike(sb,parameters,objectColumn,patternArray[1]);
            } else {
                sqlLike(sb,parameters,objectColumn,patternArray[0]);
            }
            sb.append(")");
        }
        return new PreparedSql(sb.toString(),parameters);
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
    
    protected boolean isImport(RevEngineeringOptions options, Class<? extends BaseCustomEntity> clazz) {
        return isImport(options,getObjectTypeByClass(clazz));
    }
    
    protected boolean isImport(RevEngineeringOptions options, String clazz) {
        return !options.isExcludedObjectType(clazz);
    }

    public Model getModel(String name, RevEngineeringOptions options) {
        Model model = new Model();
        model.setConnection(connector.getConnectionInfo());
        model.setName(name);
        model.setOptions(options);
        model.setLastSynch(new Date());
        model.setCustomData("dialect", getDialectName());
        model.setCustomData("dialect_version", getDialectVersion());

        if (isImport(options,Table.class)) {
            model.setTables(getTables(options).toList());
        }
        if (isImport(options,View.class)) {
            model.setViews(getViews(options).toList());
        }
        if (isImport(options,Procedure.class)) {
            model.setProcedures(getProcedures(options).toList());
        }
        if (isImport(options,Function.class)) {
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
