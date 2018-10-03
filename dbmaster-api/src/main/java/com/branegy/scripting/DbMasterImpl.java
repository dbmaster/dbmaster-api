package com.branegy.scripting;

import io.dbmaster.api.services.PublicService;

import java.sql.Connection;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import com.branegy.service.core.exception.IllegalArgumentApiException;

public class DbMasterImpl implements DbMaster {

    private final Injector injector;

    private final Logger logger;
    
    private final Set<Object> resources = Collections.synchronizedSet(
            Collections.newSetFromMap(new IdentityHashMap<Object,Boolean>()));

    private DbMasterImpl(Injector injector, Logger logger) {
        this.injector = injector;
        this.logger = logger;
    }
     
    @Override
    public <T> T getService(Class<T> serviceInterface) {
        T instance = injector.getInstance(serviceInterface);
        if (instance!=null && !(instance instanceof PublicService)) {
            throw new IllegalArgumentApiException("Can't instantiate a " +serviceInterface);
        }
        return instance;
    }

    public void closeResources() {
        synchronized (resources) {
            for (Object resource : resources) {
                try{
                    if (resource instanceof java.io.Closeable) {
                        ((java.io.Closeable)resource).close();
                    } else if (resource instanceof Context) {
                        ((Context) resource).close();
                    } else if (resource instanceof java.sql.Connection) {
                        Connection connection = (java.sql.Connection)resource;
                        if (!connection.isClosed()) {
                            try{
                                if (!connection.getAutoCommit()){
                                    connection.rollback();
                                }
                            } finally {
                                connection.close();
                            }
                        }
                    /*} else if (resource instanceof AutoCloseable){
                        ((AutoCloseable) resource).close();*/
                    } else if (resource instanceof Statement){
                        ((Statement) resource).close();
                    } else {
                        String className = resource.getClass().getCanonicalName();
                        throw new RuntimeException("Do not know how to close "+className);
                    }
                } catch (Exception e) {
                    logger.error("Cannot close {}", resource, e);
                }
            }
            resources.clear();
        }
    }
    
    public void cancelStatements(){
        synchronized (resources) {
            for (Object resource : resources) {
                try{
                    if (resource instanceof Statement) {
                        ((Statement)resource).cancel();
                    }
                } catch (SQLFeatureNotSupportedException e){
                    logger.debug("Cancel is not supported", e);
                } catch (Exception e) {
                    logger.error("Cannot abort connection {}", resource, e);
                }
            }
        }
    }

    @Override
    public void closeResourceOnExit(Object resource) {
        if (resource!=null) {
            resources.add(resource);
        }
    }
    
    public static DbMaster getInstance(Injector injector) {
        return getInstance(injector, LoggerFactory.getLogger(DbMaster.class));
    }
   
    public static DbMaster getInstance(Injector injector, Logger logger) {
        return new DbMasterImpl(injector, logger);
    }
    
    private EntityTransaction getTransaction(){
        return injector.getInstance(EntityManager.class).getTransaction();
    }
    
    @Override
    public void setRollbackOnly() {
        getTransaction().setRollbackOnly();
    }

    @Override
    public void begin() {
        getTransaction().begin();
    }

    @Override
    public void commit() {
        getTransaction().commit();
    }

    @Override
    public void rollback() {
        getTransaction().rollback();
    }

    @Override
    public boolean getRollbackOnly() {
        return getTransaction().getRollbackOnly();
    }

    @Override
    public boolean isActive() {
        return getTransaction().isActive();
    }
}