package com.branegy.util;

import java.io.File;

public abstract class DataDirHelper {

    private DataDirHelper() {
    }

    /**
     * @return return data directory with slash
     */
    public static String getDataDir(){
        String path = System.getProperty("data.dir");
        if (path!=null){
            path = new File(path).getAbsolutePath();
        } else {
            path = new File("data").getAbsolutePath();
        }
        if (!path.endsWith(File.separator)){
            path += File.separatorChar;
        }
        return path;
    }

}
