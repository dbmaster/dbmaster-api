package com.branegy.persistence;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.Hibernate;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseEntity implements IEntity {
    public static final int UPDATE_AUTHOR_LENGTH = 15;
    private static final int CREATE_AUTHOR_LENGTH = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",updatable=false)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Version
    @Column(name="updated",nullable = false)
    private Date updated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created",nullable = false, updatable = false)
    private Date created;

    @Column(name="createAuthor",length = CREATE_AUTHOR_LENGTH, updatable = false)
    @Size(min=1,max=CREATE_AUTHOR_LENGTH)
    private String createAuthor;

    @Column(name="updateAuthor",length = UPDATE_AUTHOR_LENGTH)
    @Size(min=1,max=UPDATE_AUTHOR_LENGTH)
    private String updateAuthor;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @PreUpdate
    final void preUpdate() {
        updateAuthor = CurrentUserService.getCurrentUser(CREATE_AUTHOR_LENGTH);
        // updated = new Date(); version updated by hibernate
    }

    @PrePersist
    final void prePersist(){
        preUpdate();
        created = new Date();
        createAuthor = updateAuthor;
    }

    /**
     * reset id to 0. if you make a copy of object and try to persist with old key (not 0) hibernate will
     * thrown an exception 'detached entity'
     */
    public void resetId(){
        id = 0;
    }

    @Override
    public String getCreateAuthor() {
        return createAuthor;
    }

    @Override
    public String getUpdateAuthor() {
        return updateAuthor;
    }

    /**
     * https://community.jboss.org/wiki/EqualsAndHashCode
     * http://dertompson.com/2010/05/15/equals-and-hashcode-and-hibernate/
     */
    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        } else if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)){
            return false;
        }
        return id != 0 && id == ((BaseEntity)o).id;
    }

    @Override
    public int hashCode() {
        if (id != 0) {
            return (int)(id ^ (id >>> 32));
        } else {
            return super.hashCode();
        }
    }

    public boolean isPersisted(){
        return id != 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+":"+id;
    }
}
