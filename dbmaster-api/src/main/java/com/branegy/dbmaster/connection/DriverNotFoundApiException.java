package com.branegy.dbmaster.connection;


@SuppressWarnings("serial")
public class DriverNotFoundApiException extends ConnectionApiException {
    private final DriverInfo driverInfo;

    public DriverNotFoundApiException(DriverInfo driverInfo) {
        super("Driver is not found "+driverInfo.getId()+" ["+driverInfo.getJdbcDriverClass()+"]");
        this.driverInfo = driverInfo;
    }

    public DriverInfo getDriverInfo() {
        return driverInfo;
    }
}
