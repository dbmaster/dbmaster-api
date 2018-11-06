package com.branegy.scripting;

public interface DbMaster {

    /**
     * Returns implementation instance of service.
     * @param <T> interface type
     * @param serviceInterface type of service
     * @return Instance of <code>serviceInterface</code>
     */
    <T> T getService(Class<T> serviceInterface);

    /**
     * Add resource that needs to be closed or disconnected after script execution is completed.
     * @param resource an object that keep a connection or a handle open
     */
    void closeResourceOnExit(Object resource);
    
    /**
     * Closes all resources added with closeResourceOnExit(Object resource);
     */
    void closeResources();
    

    /**
     * Set current dbmaster internal transaction to rollback-only state.
     */
    void setRollbackOnly();
    
    void begin();

    void commit();

    void rollback();

    boolean getRollbackOnly();

    boolean isActive();
}