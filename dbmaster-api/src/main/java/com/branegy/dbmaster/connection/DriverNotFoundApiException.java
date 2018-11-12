package com.branegy.dbmaster.connection;


@SuppressWarnings("serial")
public class DriverNotFoundApiException extends ConnectionApiException {
    private final ConnectorInfo driverInfo;

    public DriverNotFoundApiException(ConnectorInfo driverInfo) {
        super("Driver is not found "+driverInfo.getId()+" ["+driverInfo.getJdbcDriverClass()+"]");
        this.driverInfo = driverInfo;
    }

    public ConnectorInfo getDriverInfo() {
        return driverInfo;
    }
}
