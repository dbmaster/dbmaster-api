package com.branegy.util;

import org.slf4j.LoggerFactory;


public abstract class IOUtils {

    private IOUtils() {
    }

    public static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable!=null) {
                closeable.close();
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(IOUtils.class)
                .warn("Cannot close", e);
        }
    }
}
