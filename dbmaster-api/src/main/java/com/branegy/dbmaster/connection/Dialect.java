package com.branegy.dbmaster.connection;

/**
 * This interface represents a connection to resource.
 * Provides means to execute queries and updates.
 * 
 * TODO Should be refactored to keep connection open over multiple requests
 * @author slava
 */
public interface Dialect extends AutoCloseable{
    
    public void close();
    
}