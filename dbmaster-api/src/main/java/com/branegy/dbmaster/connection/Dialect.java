package com.branegy.dbmaster.connection;

/**
 * This interface represents a connection to resource.
 * Provides means to execute queries and updates.
 */
public interface Dialect extends AutoCloseable{
    @Override
    void close(); // do not throw any exceptions
}