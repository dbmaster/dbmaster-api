package com.branegy.inventory.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name="inv_contact_link")
@Access(AccessType.FIELD)
@CustomFieldDiscriminator("ContactLink")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_contact_link cl " +
        "inner join inv_contact c on cl.contact_id=c.id where c.project_id=:projectId")
public class ContactLink extends BaseCustomEntity{
    public static final String ROLE = "ContactRole";
    public static final String LAST_SYNC = "LastSync";

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

}
