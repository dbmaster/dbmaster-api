package com.branegy.dbmaster.custom;


import java.util.List;

import com.branegy.service.core.QueryRequest;
import com.branegy.service.core.Slice;

public interface CustomObjectService {

    List<CustomFieldConfig> findKeyFieldFor(String clazz);
    
    CustomObjectTypeEntity findCustomObjectTypeByName(String name);
    CustomObjectTypeEntity findCustomObjectTypeById(long id);
    CustomObjectTypeEntity createCustomObjectType(CustomObjectTypeEntity entity);
    CustomObjectTypeEntity updateCustomObjectType(CustomObjectTypeEntity entity);
    void deleteCustomTypeObject(CustomObjectTypeEntity entity);
    List<CustomObjectTypeEntity> getCustomObjectTypeList();
    
    CustomObjectEntity findObjectById(long id);
    CustomObjectEntity createCustomObject(CustomObjectEntity entity);
    CustomObjectEntity updateCustomObject(CustomObjectEntity entity);
    void deleteCustomObject(CustomObjectEntity entity);
    Slice<CustomObjectEntity> getCustomObjectSlice(String clazz, QueryRequest request);
}