package com.branegy.inventory.api;

import com.branegy.inventory.model.Contact;
import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface ContactService {

    void createContact(Contact contact);
    Contact saveContact(Contact contact);

    Contact findContactById(long contactid);

    Slice<Contact> getContactList(QueryRequest request);
    Slice<Contact> getContactList(int offset, int limit, String name);
    
    Contact findContactByName(String name);
    
    void deleteContact(long id);
}