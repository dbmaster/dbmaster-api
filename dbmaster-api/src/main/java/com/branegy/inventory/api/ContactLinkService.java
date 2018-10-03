package com.branegy.inventory.api;

import java.util.List;

import com.branegy.inventory.model.ContactLink;
import com.branegy.persistence.BaseEntity;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface ContactLinkService extends ContactService {

    ContactLink persistContactLink(ContactLink contactLink);

    void deleteContactLink(long id);

    ContactLink findContactLinkById(long id);

    ContactLink mergeContactLink(ContactLink contactLink);

    // TODO Should have read access to object
    List<ContactLink> findContactLinksByObject(BaseEntity object);

    Slice<ContactLink> findContactLinksByObject(BaseEntity object, QueryRequest request);

    Slice<ContactLink> findContactLinksByApplicationName(String appName, QueryRequest request);
    
    Slice<ContactLink> findContactLinksByServerName(String serverName, QueryRequest request);
    
    Slice<ContactLink> findContactLinksByJobName(String jobName, String jobType, String serverName, QueryRequest request);

    List<ContactLink> findAllByClass(Class<? extends BaseEntity> clazz, String fetchPath);
}
