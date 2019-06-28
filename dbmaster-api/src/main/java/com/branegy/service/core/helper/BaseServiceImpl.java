package com.branegy.service.core.helper;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.Table;

import org.hibernate.annotations.QueryHints;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.NativeQuery;

import com.google.common.base.Preconditions;

import com.branegy.dbmaster.core.Project;
import com.branegy.dbmaster.core.User;
import com.branegy.persistence.BaseEntity;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.service.core.exception.ConstraintViolationApiException;
import com.branegy.service.core.exception.EntityExistApiException;
import com.branegy.service.core.exception.EntityNotFoundApiException;
import com.branegy.service.core.exception.EntityVersionMismatchApiException;
import com.branegy.service.core.exception.IllegalArgumentApiException;
import com.branegy.service.core.exception.IllegalStateApiException;
import com.branegy.service.core.exception.ValidationApiException;

// TODO Move to implementation
public abstract class BaseServiceImpl {
    private static final LazyFetchInitializer INITIALIZER = new LazyFetchInitializer();

    private BaseServiceImpl() {
    }
    
    /*
     * ConstraintViolationException throws only on flush. we can do it more heuristic
     */
    private static void flushIfHasConstraint(Class<?> clazz, EntityManager em) {
        do {
            Table table = clazz.getAnnotation(Table.class);
            if (table!=null && table.uniqueConstraints().length>0){
                em.flush();
                return;
            }
            clazz = clazz.getSuperclass();
        } while (clazz!=BaseEntity.class);
    }

    /*
     * we don't need force version increment for BaseCustomEntity anymore. It's processed by
     * com.branegy.persistence.custom.CustomObjectInterceptor
     */
    public static <T extends BaseEntity> T mergeEntity(EntityManager em, T entity, Class<T> clazz) {
        if (entity == null) {
            throw new IllegalArgumentApiException(new NullPointerException());
        }
        try {
            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            flushIfHasConstraint(clazz, em);
            return entity;
        } catch (javax.validation.ConstraintViolationException e) {
            throw new ValidationApiException(e);
        } catch (OptimisticLockException e) {
            T existEntity = em.find(clazz, entity.getId());
            throw new EntityVersionMismatchApiException(e,existEntity);
        } catch (PersistenceException e) {
            ConstraintViolationException ce = findCause(e, ConstraintViolationException.class);
            if (ce != null) {
                throw new ConstraintViolationApiException(ce, entity);
            } else {
                throw new IllegalStateApiException(e);
            }
        }
    }

    public static <T extends BaseCustomEntity> T mergeCustomEntity(EntityManager em, T entity,
            Class<T> clazz) {
        return mergeEntity(em, entity, clazz);
    }

    public static <T extends BaseEntity> T persistEntity(EntityManager em, T entity) {
        ensureDetachedEntity(em, entity);
        try {
            em.persist(entity);
            return entity;
        } catch (javax.validation.ConstraintViolationException e) {
            throw new ValidationApiException(e);
        } catch (EntityExistsException e) {
            throw new EntityExistApiException();
        } catch (PersistenceException e) {
            ConstraintViolationException ce = findCause(e, ConstraintViolationException.class);
            if (ce != null) {
                throw new ConstraintViolationApiException(ce, entity);
            } else {
                throw new IllegalStateApiException(e);
            }
        }
    }

    public static <T extends BaseEntity> void deleteEntity(EntityManager em, Class<T> clazz,
            T entity) {
        T context = entity;
        if (entity == null) {
            throw new IllegalArgumentApiException(new NullPointerException());
        } else if (!em.contains(entity)) {
            try {
                context = em.getReference(clazz, entity.getId());
            } catch (EntityNotFoundException e) {
                throw new EntityNotFoundApiException(clazz,entity.getId());
            }
            if (context.getUpdated().compareTo(entity.getUpdated()) != 0) {
                throw new EntityVersionMismatchApiException(context);
            }
        }
        try {
            em.remove(context);
        } catch (RollbackException e) {
            ConstraintViolationException ce = findCause(e, ConstraintViolationException.class);
            if (ce != null) {
                throw new ConstraintViolationApiException(ce, context);
            } else {
                throw new IllegalStateApiException(e);
            }
        }
    }

    public static <T extends BaseEntity> void deleteEntity(EntityManager em, Class<T> clazz,long id) {
        T entity = null;
        try {
            entity = em.getReference(clazz, id);
            em.remove(entity);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundApiException(clazz, id);
        } catch (RollbackException e) {
            ConstraintViolationException ce = findCause(e, ConstraintViolationException.class);
            if (ce != null) {
                throw new ConstraintViolationApiException(ce, entity);
            } else {
                throw new IllegalStateApiException(e);
            }
        }
    }

    public static <T extends BaseEntity> T findEntityById(EntityManager em, Class<T> clazz, long id) {
        T entity = em.find(clazz, id);
        if (entity == null) {
            throw new EntityNotFoundApiException(clazz, id);
        }
        return entity;
    }

    public static <T extends BaseEntity> T findEntityById(EntityManager em, Class<T> clazz, long id,
            String fetchPath) {
        T entity = em.find(clazz, id);
        if (entity == null) {
            throw new EntityNotFoundApiException(clazz, id);
        }
        if (fetchPath==null || fetchPath.isEmpty()){
            return entity;
        } else {
            return initialize(em, entity, clazz, fetchPath);
        }
    }

    public static <T extends BaseEntity> T detachEntity(EntityManager em, T entity) {
        em.detach(entity);
        return entity;
    }

    public static <T extends BaseEntity> T initialize(EntityManager em, T entity,
            Class<T> clazz,String fetchPath) {
        if (fetchPath==null || fetchPath.isEmpty()){
            return entity;
        }
        INITIALIZER.fetch(em, fetchPath, clazz, entity);
        return entity;
    }

    private static <T> void ensureDetachedEntity(EntityManager em, T entity) {
        if (entity == null) {
            throw new IllegalArgumentApiException(new NullPointerException());
        } else if (em.contains(entity)) {
            throw new IllegalArgumentApiException();
        }
    }
    
    public static void ensureCurrentProject(Project fromBean, Project currenProject){
        Preconditions.checkNotNull(currenProject, "Current project can not be null");
        Preconditions.checkArgument(fromBean == null || fromBean.getId() == currenProject.getId(),
                "Project id is different %s and %s",fromBean,currenProject);
    }
    
    public static void ensureCurrentUser(User fromBean, User currenUser){
        Preconditions.checkNotNull(currenUser, "Current user can not be null");
        Preconditions.checkArgument(fromBean == null || fromBean.getId() == currenUser.getId(),
                "User id is different %s and %s",fromBean,currenUser);
    }
    
    public static void ensureUserIsNotSet(User fromBean){
        Preconditions.checkArgument(fromBean == null, "Current user should be null");
    }

    // http://stackoverflow.com/questions/6812370/detached-list-from-a-query
    // JPA compatible, for query use a hint QueryHints.READ_ONLY=true
    public static <T> List<T> detachList(EntityManager em, List<T> sourceList) {
        for (T t : sourceList) {
            em.detach(t);
        }
        return Collections.unmodifiableList(sourceList);
    }

    public static <T> List<T> initializeList(EntityManager em,List<T> sourceList,
            Class<T> clazz,String fetchPath) {
        if (fetchPath==null || fetchPath.isEmpty()){
            return Collections.unmodifiableList(sourceList);
        }
        for (T t : sourceList) {
            INITIALIZER.fetch(em, fetchPath, clazz, t);
        }
        // TODO - clarify why we need here unmodifiableList,
        // e.g. ContactLinkServiceImpl.findAllByClass don't need to return unmodifiableList
        return Collections.unmodifiableList(sourceList);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Exception> T findCause(Throwable e, Class<T> causeClazz) {
        while (e != null && e.getClass() != causeClazz) {
            e = e.getCause();
        }
        return (T) e;
    }

    public static <T extends Query> T markQueryResultReadonly(T query){
        query.setHint(QueryHints.READ_ONLY, Boolean.TRUE);
        return query;
    }
    
    public static void disableInvalidateCache(Query query){
        query.unwrap(NativeQuery.class).addSynchronizedQuerySpace("");
    }
    
    public static <T extends BaseEntity> T getUnchangedObject(EntityManager em,Class<T> clazz, T object){
        EntityManager entityManager = null;
        try{
            entityManager = em.getEntityManagerFactory().createEntityManager();
            return em.find(clazz, object.getId());
        } finally {
            if (entityManager!=null){
                entityManager.close();
            }
        }
    }

}
