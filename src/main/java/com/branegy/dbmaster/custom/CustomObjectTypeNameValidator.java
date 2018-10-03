package com.branegy.dbmaster.custom;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.ImmutableList;

public class CustomObjectTypeNameValidator implements
        ConstraintValidator<CustomObjectTypeNameValidation, CustomObjectTypeEntity> {
    private final List<String> INVENTORY_SYSTEM_NAMES = ImmutableList.of("application", "installation",
            "contactlink", "contact", "database", "databaseusage", "server", "connection");
    private final List<String> MODELING_SYSTEM_NAMES = ImmutableList.of("model", "table", "column",
            "view", "procedure", "function", "parameter");

    @Override
    public void initialize(CustomObjectTypeNameValidation anno) {
    }

    @Override
    public boolean isValid(CustomObjectTypeEntity o, ConstraintValidatorContext ctx) {
        List<String> system;
        if ("INVENTORY".equals(o.getProject().getType())){
            system = INVENTORY_SYSTEM_NAMES;
        } else if ("MODELING".equals(o.getProject().getType())){
            system = MODELING_SYSTEM_NAMES;
        } else {
            throw new IllegalStateException("Unknown project type "+o.getProject().getType());
        }
        return !system.contains(o.getObjectName().toLowerCase());
    }

}
