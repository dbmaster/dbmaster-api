package com.branegy.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides interface to read from and write to files.
 */
public interface FileReference {
    
    /**
     * Returns reference identifier.
     * @return
     */
    long getId();

    /**
     * Returns name of the file
     */
    String getName();

    
    /**
     * Returns size of the file in bytes
     */
    long getSize();
    
    /**
     * Use this method when you want to write some content to the file.
     * @return
     * @throws IOException
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Use this method when you want to read content of the file.
     * @return
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;

}
