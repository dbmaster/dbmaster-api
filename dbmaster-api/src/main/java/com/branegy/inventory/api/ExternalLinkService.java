package com.branegy.inventory.api;

import java.util.List;

import com.branegy.inventory.model.ExternalLink;
import com.branegy.persistence.BaseEntity;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface ExternalLinkService{

    ExternalLink findExternalLinkById(long id);
    
    ExternalLink createExternalLink(ExternalLink externalLink);

    ExternalLink updateExternalLink(ExternalLink externalLink);
    
    void deleteExternalLink(long id);

    // TODO Should have read access to object
    List<ExternalLink> findExternalLinksByObject(BaseEntity object);

    Slice<ExternalLink> findExternalLinksByObject(BaseEntity object, QueryRequest request);

    List<ExternalLink> findAllByClass(Class<? extends BaseEntity> clazz, String fetchPath);
}
