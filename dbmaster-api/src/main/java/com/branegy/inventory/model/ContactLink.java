package com.branegy.inventory.model;

import static com.branegy.persistence.custom.EmbeddableKey.CLAZZ_COLUMN;
import static com.branegy.persistence.custom.EmbeddableKey.ENTITY_ID_COLUMN;

import java.util.SortedMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="inv_contact_link")
@Access(AccessType.FIELD)
@CustomFieldDiscriminator(ContactLink.CUSTOM_FIELD_DISCRIMINATOR)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_contact_link cl " +
        "inner join inv_contact c on cl.contact_id=c.id where c.project_id=:projectId")
public class ContactLink extends BaseCustomEntity{
    static final String CUSTOM_FIELD_DISCRIMINATOR = "ContactLink";
    public static final String ROLE = "ContactRole";
    public static final String LAST_SYNC_DATE = "Last Sync Date";

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="application_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Application application;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="database_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Database database;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="server_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Server server;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="job_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Job job;

    @ManyToOne(optional=false)
    @JoinColumn(name="contact_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    Contact contact;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    @Access(AccessType.PROPERTY)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name=BaseCustomEntity.CUSTOMFIELD_VALUE_TABLE, joinColumns = {@JoinColumn(name=ENTITY_ID_COLUMN)})
    @BatchSize(size = 100)
    @Where(clause=CLAZZ_COLUMN+" = '"+CUSTOM_FIELD_DISCRIMINATOR+"'")
    @SortNatural
    protected SortedMap<EmbeddableKey, EmbeddablePrimitiveContainer> getMap() {
        return getInnerCustomMap();
    }
}
