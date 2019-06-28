package com.branegy.inventory.model;

import static com.branegy.inventory.model.Server.QUERY_SERVER_FIND_ALL;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Where;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.EmbeddableKey;
import com.branegy.persistence.custom.EmbeddablePrimitiveContainer;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;

@Entity
@Table(name = "inv_server")
@CustomFieldDiscriminator(Server.CUSTOM_FIELD_DISCRIMINATOR)
@NamedQueries({
    @NamedQuery(name = QUERY_SERVER_FIND_ALL,
            query = "select s from Server s where s.project.id=:project_id")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@FetchAllObjectIdByProjectSql("select id from inv_server where project_id=:projectId")
public class Server extends BaseCustomEntity {
    static final String CUSTOM_FIELD_DISCRIMINATOR = "Server";

    public static final String QUERY_SERVER_FIND_ALL = "findAllServers";

    public static final String SERVER_NAME = "ServerName";

    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    Project project;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getServerName(){
        return getCustomData(SERVER_NAME);
    }

    public void setServerName(String serverName){
        setCustomData(SERVER_NAME,serverName);
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