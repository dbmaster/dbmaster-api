package com.branegy.files;

import io.dbmaster.api.services.PublicService;

import java.net.URL;


/**
 * Provides access to files. Files can be stored on a file system or in the cloud.
 * Folders are not supported.
 */
public interface FileService extends PublicService {

    /**
     * Retrieves reference to a file by fileName
     */
    FileReference getFile(String fileName);
    
    /**
     * Creates a reference for a new file.
     * Use FileReference.getOutputStream() to set content for the file.
     * Requires user to be a member of a contributer or full roles.
     * @param fileName
     * @param toolId - optional parameter
     * @return
     */
    FileReference createFile(String fileName, String toolId);
    
    /**
     * Deletes the file.
     * Requires user to be a member of a contributer or full roles.
     * @param fileReferece
     */
    void deleteFile(FileReference fileReferece);
    
    /**
     * Returns a url to the file
     * Property WebSecurityUtil.DBMASTER_URL (dbmaster.url) is used as a prefix
     */
    URL toURL(FileReference fileReferece);
}
