package com.branegy.dbmaster.custom;

import static com.branegy.dbmaster.custom.CustomFieldConfig.QUERY_FIND_ALL_FOR_PROJECT;
import static com.branegy.dbmaster.custom.CustomFieldConfig.QUERY_FIND_BY_PROJECT_CLAZZ_NAME;
import static com.branegy.dbmaster.custom.CustomFieldConfig.QUERY_FIND_KEY_FIELD;
import static com.branegy.dbmaster.custom.CustomFieldConfig.QUERY_GLOBAL_CUSTOM_CONFIG;
import static com.branegy.dbmaster.custom.CustomFieldConfig.QUERY_PERSONAL_PROJECT_CUSTOM_CONFIG;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;
import com.branegy.persistence.BaseEntity;

/**
 * represent custom field config
 *
 */
@Entity
@Table(name="customfield_config", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "name", "clazz" })
})
@Access(AccessType.FIELD)
@NamedQueries({
    /**
     * global custom config only
     */
    @NamedQuery(name = QUERY_GLOBAL_CUSTOM_CONFIG, query = "select c from CustomFieldConfig c " +
            "where c.project is null and c.user is null and c.domain=:domain order by c.id asc"),
    /**
     * project and personal field for user (domain not used, load from project)
     */
    @NamedQuery(name = QUERY_PERSONAL_PROJECT_CUSTOM_CONFIG, query = "select c from CustomFieldConfig c " +
            "where c.project.id=:projectId and (c.user is null or c.user.id=:userId) order by c.id asc"),
    /**
     * global,project and personal field for all user for project
     */
    @NamedQuery(name = QUERY_FIND_ALL_FOR_PROJECT, query = "select c from CustomFieldConfig c " +
            "where c.project.id=:projectId order by c.id asc"),

    @NamedQuery(name = QUERY_FIND_BY_PROJECT_CLAZZ_NAME, query = "from CustomFieldConfig c " +
            "where c.project.id=:projectId and clazz=:clazz and name=:name"),
    @NamedQuery(name = QUERY_FIND_KEY_FIELD, query = "from CustomFieldConfig " +
            "where key = true and customObjectType is not null and " +
                  "clazz=:customObjectType and project=:project " +
            "order by upper(name)")
})
public class CustomFieldConfig extends BaseEntity {
    public static final String QUERY_FIND_KEY_FIELD = "CustomFieldConfig.findKeyField";
    public static final String QUERY_FIND_BY_PROJECT_CLAZZ_NAME = "CustomFieldConfig.findByProjectClazzName";
    public static final String QUERY_FIND_ALL_FOR_PROJECT = "CustomFieldConfig.findAllForProject";
    public static final String QUERY_PERSONAL_PROJECT_CUSTOM_CONFIG =
            "CustomFieldConfig.getPersonalProjectCustomConfig";
    public static final String QUERY_GLOBAL_CUSTOM_CONFIG = "CustomFieldConfig.getGlobalCustomConfig";

    /**
     * using in tools too! make more generic and get rid of presentation level
     */
    // TODO Possibly move to a public "api" package
    public static enum Type {
        INTEGER, FLOAT, DATE, BOOLEAN, TEXT, HTML, STRING,
        FILE_REF;
    };

    @ManyToOne(fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id",updatable=false)
    private Project project;

    @Column(name="name", nullable=false, length = 255)
    @NotNull
    private String name;

    @Column(name="clazz", nullable=false, updatable=false, length=32)
    @NotNull
    private String clazz;

    @Column(name="required")
    private boolean required;

    /**
     * readonly field
     */
    @Column(name="system", updatable=false)
    private boolean system;

    @Column(name="readonly")
    private boolean readonly;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable=false, length=255)
    @NotNull
    private Type type;

    @Column(name="description",length=2000)
    private String description;

    @Column(name="minDecimal")
    private Long minDecimal;
    @Column(name="maxDecimal")
    private Long maxDecimal;

    @Column(name="regexp",length=255)
    private String regexp;

    @ElementCollection(fetch=FetchType.EAGER)
    @Column(name="textValues", length = 255)
    @CollectionTable(name="customfield_config_values_text")
    private List<String> textValues;
    
    @Transient
    private List<Long> integerValues;
    @Transient
    private List<Double> fractionalValues;
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    private List<Date> timeValues;

    /**
     * typically project type
     * not null for GLOBAL config only, else domain fetching as project.projectType
     */
    @Column(name="domain", updatable = false, length = 16)
    private String domain;

    @Column(name="suggestion", length=32)
    private String suggestion;

    @Column(name="key_field")
    private boolean key;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="custom_object_type",updatable=false)
    private CustomObjectTypeEntity customObjectType;
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMinDecimal() {
        return minDecimal;
    }

    public void setMinDecimal(Long minDecimal) {
        this.minDecimal = minDecimal;
    }

    public Long getMaxDecimal() {
        return maxDecimal;
    }

    public void setMaxDecimal(Long maxDecimal) {
        this.maxDecimal = maxDecimal;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public List<String> getTextValues() {
        if (textValues == null){
            textValues = new ArrayList<String>();
        }
        return textValues;
    }

    public void setTextValues(List<String> textValues) {
        this.textValues = textValues;
    }
    
    public List<Long> getIntegerValues() {
        return integerValues;
    }

    public void setIntegerValues(List<Long> integerValues) {
        this.integerValues = integerValues;
    }

    public List<Double> getFractionalValues() {
        return fractionalValues;
    }

    public void setFractionalValues(List<Double> fractionalValues) {
        this.fractionalValues = fractionalValues;
    }

    public List<Date> getTimeValues() {
        return timeValues;
    }

    public void setTimeValues(List<Date> timeValues) {
        this.timeValues = timeValues;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public CustomObjectTypeEntity getCustomObjectType() {
        return customObjectType;
    }

    public void setCustomObjectType(CustomObjectTypeEntity customObjectType) {
        this.customObjectType = customObjectType;
    }

    // TODO permission (read/write)

}
