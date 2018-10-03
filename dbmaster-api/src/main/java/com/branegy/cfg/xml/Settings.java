package com.branegy.cfg.xml;

import java.io.Serializable;
import java.util.Properties;

// TODO config can't be null, rewrite code

@SuppressWarnings("serial")
public class Settings implements Serializable {
    private String license;
    private Properties config;

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setProperty(String key, String value) {
        if (config == null) {
            config = new Properties();
        }
        config.put(key, value);
    }

    public Object getProperty(String key) {
        if (config == null) {
            config = new Properties();
        }
        return config.get(key);
    }

    public String getProperty(String key, String def) {
        if (config == null) {
            config = new Properties();
        }
        String value = config.getProperty(key);
        return value != null ? value : def;
    }

    public void setProperties(Properties properties) {
        if (properties!=null) {
            if (config == null) {
                config = new Properties();
            }
            config.putAll(properties);
        }
    }

    public Properties toProperties(){
        return config;
    }

}