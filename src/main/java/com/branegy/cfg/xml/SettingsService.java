package com.branegy.cfg.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branegy.util.DataDirHelper;


/**
 * @deprecated  use IPropertySupplier interface
 */
@Deprecated
public class SettingsService {
    protected static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    private static SettingsService instance;

    private Settings settings;
    private final String folderName;

    // TODO (Vitaly) Explain alternative/new way to load settings
    public synchronized static SettingsService instance() {
        if (instance == null) {
            instance = new SettingsService(DataDirHelper.getDataDir());
        }
        return instance;
    }

    private SettingsService(String folderName) {
        this.folderName = folderName;
        try {
            File configFile = new File(folderName + "/config.ini");
            if (configFile.exists()) {
                Properties tempProperties = new Properties();
                FileInputStream file = new FileInputStream(configFile);
                tempProperties.load(file);
                file.close();
                settings = new Settings();
                settings.setProperties(tempProperties);
                logger.info("Loading configuration from "+ configFile.getCanonicalPath());
            } else {
                logger.error("config.ini file does not exist in {}",new File(folderName).getCanonicalPath());
                settings = new Settings();
            }
        } catch (FileNotFoundException e) {
            try {
                logger.error("Neither config.xml nor config.ini files exist in {}",
                        new File(folderName).getCanonicalPath());
            } catch (IOException e1) {
                logger.error("",e);
            }
            settings = new Settings();
        } catch (IOException e) {
            logger.error("config.ini file contains errors. Please correct", e);
            settings = new Settings();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, String> asMap() {
        return (Map) settings.toProperties();
    }

    public void setProperty(String key, String value) throws IOException {
        settings.setProperty(key, value);
        saveData(settings);
    }

    @SuppressWarnings("unchecked")
    public <X> X getProperty(String key) {
        return (X) settings.getProperty(key);
    }

    protected void saveData(Object settings) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(folderName + "/config.ini");
            ((Settings) settings).toProperties().store(fos, null);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

}
