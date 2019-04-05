package com.branegy.service.core;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import com.branegy.service.core.exception.AuthenticationApiException;

import io.dbmaster.api.services.PublicService;

public abstract class AbstractService implements PublicService {
    @Inject
    protected Injector injector;

    @Inject
    private Provider<EntityManager> emProvider;

    private ISecurityContext context;
    
    @Inject
    private void injectSecurityContext(Provider<ISecurityContext> provider) {
        try {
            context = provider.get();
        } catch (ProvisionException e) {
            if (e.getCause() instanceof AuthenticationApiException){
                context = ISecurityContext.DEFAULT;
            } else {
                throw e;
            }
        }
    }
    
    /**
     * return actual security context.
     */
    public ISecurityContext getContext() {
        return context;
    }

    protected EntityManager getEm(){
        return emProvider.get();
    }

}
