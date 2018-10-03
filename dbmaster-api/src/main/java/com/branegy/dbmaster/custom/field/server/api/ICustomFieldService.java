package com.branegy.dbmaster.custom.field.server.api;

import java.util.List;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;
import com.branegy.dbmaster.custom.CustomFieldConfig;
import com.branegy.dbmaster.custom.CustomObjectTypeEntity;
import com.branegy.persistence.custom.BaseCustomEntity;

public interface ICustomFieldService {
    List<CustomFieldConfig> getTemplateCustomConfigList(String domain);

    List<CustomFieldConfig> getProjectCustomConfigList(Project project, User user);

    List<CustomFieldConfig> getProjectCustomConfigList();

    CustomFieldConfig getConfigByName(String className, String name);

    void copyTemplateConfigTo(Project project);

    CustomFieldConfig persistTemplateCustomFieldConfig(CustomFieldConfig config);

    void deleteTemplateCustomFieldConfig(long id);

    CustomFieldConfig mergeCustomFieldConfig(CustomFieldConfig config);

    CustomFieldConfig findById(long id);
    
    
    
    void createCustomFieldConfig(Class<? extends BaseCustomEntity> clazz, CustomFieldConfig config, boolean personal);

    void createCustomFieldConfig(CustomObjectTypeEntity type, CustomFieldConfig config, boolean personal);

    CustomFieldConfig updateCustomFieldConfig(CustomFieldConfig config);

    void deleteCustomFieldConfig(long id);
}
