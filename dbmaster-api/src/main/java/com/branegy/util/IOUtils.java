package com.branegy.util;

import java.math.BigInteger;

import org.slf4j.LoggerFactory;


public abstract class IOUtils {
    private static final BigInteger THOUSAND = BigInteger.valueOf(1000);
    private static final BigInteger ONE_KB = BigInteger.valueOf(1024);
    private static final String[] FILE_SIZE_UNITS = new String[] {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };

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
    
    public static String toFileSize(BigInteger size) {
        if (size==null) {
            return "---";
        } else {
            BigInteger value = size;
            BigInteger mod = BigInteger.ZERO;
            int index = 0;
            while(index < FILE_SIZE_UNITS.length) {
                if (value.compareTo(ONE_KB)<0) {
                    break;
                }
                value = value.divide(ONE_KB);
                mod = value.mod(ONE_KB);
                index++;
            }
            mod = mod.divide(THOUSAND);
            int modInteger = Math.round(mod.floatValue()/10);
            return value + (index == 0?" ":"," + modInteger+" ") + FILE_SIZE_UNITS[index];
        }
    }
    
    public static String toFileSize(String size) {
        return toFileSize(size == null? null : new BigInteger(size));
    }
}
