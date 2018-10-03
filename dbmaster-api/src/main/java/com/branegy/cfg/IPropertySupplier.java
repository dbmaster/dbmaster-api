package com.branegy.cfg;

import java.util.Map;

// TODO (Vitali) add method for int / long / boolean
public interface IPropertySupplier {
    String getProperty(String name);

    String getProperty(String name, String defaultValue);

    void setProperty(String name, String value);

    Map<String, String> getProperties(String prefix, boolean stripPrefix);

    Map<String, String> asMap();
}
