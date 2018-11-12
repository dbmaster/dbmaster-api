package com.branegy.dbmaster.model;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

// TODO use setter + getter
@Embeddable
public class RevEngineeringOptions {

    @Size(min=1,max=255)
    public String database;

    public boolean importTables = true;
    @Size(min=1,max=255)
    public String includeTables;
    @Size(min=1,max=255)
    public String excludeTables;

    public boolean importIndexes = true;
    public boolean importConstraints = true;
    public boolean importForeignKeys = true;
    @Transient
    public boolean importTriggers = true;


    public boolean importViews;
    @Size(min=1,max=255)
    public String includeViewsFilter;
    @Size(min=1,max=255)
    public String excludeViewsFilter;

    public boolean importProcedures;
    @Size(min=1,max=255)
    public String includeProcedureFilter;
    @Size(min=1,max=255)
    public String excludeProcedureFilter;

}
