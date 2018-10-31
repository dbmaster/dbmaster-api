package com.branegy.dbmaster.connection;

// TODO change setter visibility to package/protected
public class ConnectorInfo {
    /**
     * Connection will refer to it
     */
    private String id;

    /**
     * driver title - will appear in dropdown for user
     */
    private String name;

   /**
    * sometimes it is necessary to disable drivers.
    * (e.g. we will include mysql driver description but disabled)
    */
    private boolean enabled;

    /**
     * driver class path - connection manager will use it to load class (optional parameter)
     */
    private String connectorClassPath;

    /**
     * jdbc driver - full qualified class name
     */
    private String jdbcDriverClass;

    /**
     * Text to display to user under database url input box
     */
    private String urlFormat;
    
    
    /**
     * Defines property name that can be used to set initial database name
     */
    private String databaseNameProperty;
    
    private ConnectorProperty[] properties;

    /**
     * jdbc driver - full qualified class name
     */
    private String connectorClass;
    
    private String installationTip;
    
    private transient boolean loaded;

    public static class ConnectorProperty {
        private String key;

        private String value;

        private String description;

        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConnectorClassPath() {
        return connectorClassPath;
    }

    public void setConnectorClassPath(String driverClassPath) {
        this.connectorClassPath = driverClassPath;
    }

    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    public void setJdbcDriverClass(String driverClass) {
        this.jdbcDriverClass = driverClass;
    }

    public String getUrlFormat() {
        return urlFormat;
    }

    public void setUrlFormat(String urlFormat) {
        this.urlFormat = urlFormat;
    }

    public ConnectorProperty[] getProperties() {
        return properties;
    }

    public void setProperties(ConnectorProperty[] properties) {
        this.properties = properties;
    }

    public String getConnectorClass() {
        return connectorClass;
    }

    public void setConnectorClass(String connectorClass) {
        this.connectorClass = connectorClass;
    }

    public String getDatabaseNameProperty() {
        return databaseNameProperty;
    }

    public void setDatabaseNameProperty(String databaseNameProperty) {
        this.databaseNameProperty = databaseNameProperty;
    }

    public String getInstallationTip() {
        return installationTip;
    }

    public void setInstallationTip(String installationTip) {
        this.installationTip = installationTip;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

}
