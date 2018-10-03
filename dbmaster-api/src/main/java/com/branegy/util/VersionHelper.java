package com.branegy.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branegy.cfg.xml.SettingsService;
import com.branegy.service.core.exception.IllegalArgumentApiException;

public abstract class VersionHelper {
    public static final Logger logger = LoggerFactory.getLogger(VersionHelper.class);
    
    public static class Version implements Comparable<Version> {
        private final int major;
        private final int minor;
        private final int revision;

        private final String buildDate;
        private final String productName;

        public Version(String version, String buildDate) {
            this(version, buildDate, null);
        }
        
        public Version(String version, String buildDate, String productName) {
            String[] split = version.split("\\.");
            this.major = Integer.parseInt(split[0]);
            this.minor = Integer.parseInt(split[1]);
            this.revision = Integer.parseInt(split[2]);
            this.buildDate = buildDate;
            this.productName = productName;
        }
        
        public Version(int major,int minor,int revision, String buildDate) {
            this.major = major;
            this.minor = minor;
            this.revision = revision;
            this.buildDate = buildDate;
            this.productName = null;
        }

        public String getBuildDate() {
            return buildDate;
        }
        
        public String getProductName() {
            return productName;
        }

        protected static Manifest getManifest(Class<?> clazz) {
            try {
                Enumeration<URL> resources = clazz.getClassLoader().getResources("META-INF/MANIFEST.MF");
                Manifest manifest = null;
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    InputStream stream =null;
                    try{
                        stream = url.openStream();
                        manifest = new Manifest(stream);
                        if (manifest.getMainAttributes().getValue("Version")!=null &&
                            manifest.getMainAttributes().getValue("Build-Date")!=null) {
                            break;
                        } else {
                            manifest = null;
                        }
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }
                return manifest;
            } catch (java.net.MalformedURLException e) {
                throw new IllegalArgumentException(e);
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        }

        protected static Version getVersion(Class<?> clazz) {
            try {
                Manifest manifest = getManifest(clazz);
                if (manifest==null){
                    return new Version("0.0.0", null);
                }
                Attributes attributes = manifest.getMainAttributes();
                String versionValue = attributes.getValue("Version");
                return new Version(versionValue, attributes.getValue("Build-Date"));
            } catch (Exception e) {
                LoggerFactory.getLogger(Version.class).error("Cannot load version", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + revision;
        }

        @Override
        public int compareTo(Version otherVersion) {
            int r = Integer.valueOf(this.major).compareTo(otherVersion.major);
            if (r == 0) {
                r = Integer.valueOf(this.minor).compareTo(otherVersion.minor);
                if (r == 0) {
                    return Integer.valueOf(this.revision).compareTo(otherVersion.revision);
                }
            }
            return r;
        }

        public boolean isPrecedes(Version otherVersion) {
            return compareTo(otherVersion) < 0;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getRevision() {
            return revision;
        }

    }

    
    private VersionHelper() {
    }

    public static String checkUpdate(){
        final String NO_UPDATES_AVAILABLE = null;

        String updateURL = SettingsService.instance().getProperty("update.url");
        if (updateURL == null) {
            logger.info("Skipping new version check. Property update.url is not set.");
            return NO_UPDATES_AVAILABLE;
        }
        if (!updateURL.endsWith("version.txt")) {
            if (!updateURL.endsWith("/")) {
                updateURL+="/";
            }
            updateURL+="version.txt";
        }
        try {
            URL url = new URL(updateURL);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            InputStream openStream = connection.getInputStream();
            Version availableVersion = VersionHelper.getVersion(openStream);
            Version currentVersion = VersionHelper.getAppVersion();

            if (availableVersion == null || currentVersion == null) {
                logger.info("No updates available {} for {} at {}", availableVersion, currentVersion,
                        updateURL);
                return NO_UPDATES_AVAILABLE;
            } else if (currentVersion.isPrecedes(availableVersion)) {
                logger.info("Updates available {} for {} at {}", availableVersion, currentVersion, updateURL);
                return availableVersion.toString();
            } else {
                logger.info("No updates available for {} at {}", currentVersion, updateURL);
                return NO_UPDATES_AVAILABLE;
            }
        } catch (Exception e) {
            logger.info("Update site is not available: " + e.getMessage());
            return NO_UPDATES_AVAILABLE;
        }
    }

    public static Version getAppVersion(){
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(DataDirHelper.getDataDir() + "/../version.txt");
            return VersionHelper.getVersion(stream);
        } catch (IOException e){
            throw new IllegalArgumentApiException(e);
        } finally{
            IOUtils.closeQuietly(stream);
        }
    }

    private static Version getVersion(InputStream stream) throws IOException {
        Properties versionFile = new Properties();
        versionFile.load(stream);
        String versionInfo = versionFile.getProperty("version.info");
        String productName = versionFile.getProperty("product.name");
        if (versionInfo != null) {
            return new Version(versionInfo, null, productName);
        } else {
            logger.error("version.info property not found");
            return null;
        }
    }

    protected static java.net.URL getCodeLocation(Class<?> clazz) {
        java.security.ProtectionDomain pd = clazz.getProtectionDomain();
        java.security.CodeSource cs = pd.getCodeSource();
        java.net.URL url = cs.getLocation();
        return url;
    }

    protected static Manifest getManifest(java.net.URL location) {
        final String MANIFEST = "META-INF/MANIFEST.MF";
        try {
            java.net.URL manifestUrl = new java.net.URL(location.toExternalForm() + MANIFEST);
            InputStream stream = manifestUrl.openStream();
            Manifest manifest = new Manifest(stream);
            stream.close();
            return manifest;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
