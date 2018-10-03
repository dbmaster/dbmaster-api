package com.branegy.service.core;

import javax.persistence.EntityManager;

import io.dbmaster.api.services.PublicService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public abstract class AbstractService implements PublicService {
    @Inject
    protected Injector injector;

    @Inject
    private Provider<EntityManager> emProvider;

    @Inject
    private ISecurityContext context;

    // TODO (Vitaly) must be protected
    public ISecurityContext getContext() {
        return context;
    }

    protected EntityManager getEm(){
        return emProvider.get();
    }

}
