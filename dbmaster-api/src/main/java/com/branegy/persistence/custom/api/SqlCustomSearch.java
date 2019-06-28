package com.branegy.persistence.custom.api;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.branegy.persistence.BaseEntity;

public interface SqlCustomSearch<T extends BaseEntity> {

    Query getQuery(EntityManager em);
    List<T> getList(EntityManager em);

    Query getCountQuery(EntityManager em);
    int getCount(EntityManager em);
}