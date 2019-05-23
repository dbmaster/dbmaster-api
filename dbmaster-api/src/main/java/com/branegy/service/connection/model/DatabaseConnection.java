package com.branegy.service.connection.model;

import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_ALL;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_ALL_BY_PROJECT;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_ALL_COUNT;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_ALL_ENABLED_BY_PROJECT;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_ALL_ENABLED_COUNT;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_ALL_PAGE_COUNT;
import static com.branegy.service.connection.model.DatabaseConnection.QUERY_CONNECTION_FIND_FULL_NAME;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.branegy.dbmaster.core.Project;
import com.branegy.persistence.custom.BaseCustomEntity;
import com.branegy.persistence.custom.CustomFieldDiscriminator;
import com.branegy.persistence.custom.FetchAllObjectIdByProjectSql;
import com.branegy.persistence.xml.XmlBlobArray;

@Entity
@NamedQueries({
    @NamedQuery(name = QUERY_CONNECTION_FIND_ALL_BY_PROJECT, query = "select c from DatabaseConnection c " +
            "where c.project.id=:projectId"),
    @NamedQuery(name = QUERY_CONNECTION_FIND_ALL_ENABLED_BY_PROJECT, query = "select c from DatabaseConnection c " +
            "where c.project.id=:projectId and c.disabled = false"),
    @NamedQuery(name = QUERY_CONNECTION_FIND_ALL, query = "from DatabaseConnection c " +
            "order by c.project.name asc, c.name asc"),
    @NamedQuery(name = QUERY_CONNECTION_FIND_ALL_COUNT, query = "select count(c) from DatabaseConnection c"),
    @NamedQuery(name = QUERY_CONNECTION_FIND_ALL_ENABLED_COUNT, query = "select count(c) from DatabaseConnection c "+
            "where c.disabled = false"),
    @NamedQuery(name = QUERY_CONNECTION_FIND_ALL_PAGE_COUNT,
            query = "select count(c) from DatabaseConnection c " +
                "where c.project.name || '.' || c.name < :query"),
    @NamedQuery(name = QUERY_CONNECTION_FIND, query = "select c from DatabaseConnection c " +
            "where upper(c.name)=upper(:name) and c.project.id=:projectId"),
    @NamedQuery(name = QUERY_CONNECTION_FIND_FULL_NAME,
            query = "from DatabaseConnection c where c.project.name || '.' || c.name = :name")
})
@Table(name="db_connection",uniqueConstraints={@UniqueConstraint(columnNames={"name","project_id"})})
@CustomFieldDiscriminator("Connection")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name="to-xml-array",typeClass=XmlBlobArray.class,parameters={
    @Parameter(name = "alias.connection",
               value = "property,com.branegy.service.connection.model.DatabaseConnection$PropertyInfo")
})
@FetchAllObjectIdByProjectSql("select id from db_connection where project_id=:projectId")
public class DatabaseConnection extends BaseCustomEntity {
    public static final String QUERY_CONNECTION_FIND = "Connection.find";
    public static final String QUERY_CONNECTION_FIND_ALL_BY_PROJECT = "Connection.findAllByProject";
    public static final String QUERY_CONNECTION_FIND_ALL_ENABLED_BY_PROJECT = "Connection.findAllEnabledByProject";
    public static final String QUERY_CONNECTION_FIND_ALL = "Connection.findAll";
    public static final String QUERY_CONNECTION_FIND_ALL_COUNT = "Connection.findAllCount";
    public static final String QUERY_CONNECTION_FIND_ALL_ENABLED_COUNT = "Connection.findAllEnabledCount";
    public static final String QUERY_CONNECTION_FIND_ALL_PAGE_COUNT = "Connection.findAllPageCount";
    public static final String QUERY_CONNECTION_FIND_FULL_NAME = "Connection.findByFullName";
    
    public static final String LAST_SYNC = "LastSync";
    public static final String SYNC_EXCLUDE_JOBS = "Sync.ExcludeJobs";
    
    public static final String ENCRYPT_KEY_CONSTANT =
            "com.branegy.service.connection.model.DatabaseConnection.ENCRYPT_KEY";

    // key for password encrypting
    @Inject @Named(ENCRYPT_KEY_CONSTANT)
    private transient static String ENCRYPT_KEY;// = "m3sd5Ed6s78gVkes"; legacy value

    private static final class Blowfish {

        public static String encryptBlowfish(String to_encrypt, String strkey) {
            try {
                SecretKeySpec key = new SecretKeySpec(strkey.getBytes(StandardCharsets.UTF_8), "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] doFinal = cipher.doFinal(to_encrypt.getBytes(StandardCharsets.UTF_8));
                return new BigInteger(1,doFinal).toString(16);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static String decryptBlowfish(String to_decrypt, String strkey) {
            try {
                SecretKeySpec key = new SecretKeySpec(strkey.getBytes(StandardCharsets.UTF_8), "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] byteArray = new BigInteger(to_decrypt, 16).toByteArray();
                byte[] decrypted;
                if (byteArray[0]==0) {
                    decrypted = cipher.doFinal(byteArray, 1 ,byteArray.length-1);
                } else {
                    decrypted = cipher.doFinal(byteArray);
                }
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @SuppressWarnings("serial")
    public static final class PropertyInfo implements Serializable{
        private String key;
        private String value;
        private String description;

        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
        public String getDescription() {
            return description;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof PropertyInfo))
                return false;
            PropertyInfo other = (PropertyInfo) obj;
            if (description == null) {
                if (other.description != null)
                    return false;
            } else if (!description.equals(other.description))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
    }

    @Column(name="name", nullable=false, length=255)
    @NotNull
    @Pattern(regexp="^[^\\.]{1,255}$")
    private String name;

    @Column(name="driver",length=255)
    @NotNull
    @Size(min=1,max=255)
    private String driver;

    @Column(name="username",length=255)
    @Size(max=255)
    private String username;

    @Access(AccessType.PROPERTY)
    @Column(name="password",length=255)
    @Size(max=255)
    private String password;

    @Column(name="url",length=255)
    @Size(max=255)
    private String url;

    @Column(name="properties")
    @Lob
    @Type(type="to-xml-array")
    private PropertyInfo[] properties;

    @ManyToOne(optional = false,fetch=FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="project_id")
    private Project project;
    
    @Access(AccessType.PROPERTY)
    @Column(name="disabled", nullable=false)
    private boolean disabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password!=null ? Blowfish.decryptBlowfish(password, ENCRYPT_KEY) : "";
    }

    public void setPassword(String password) {
        this.password = password!=null ? Blowfish.encryptBlowfish(password, ENCRYPT_KEY) : null;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public Properties asProperties(){
        Properties properties = new Properties();
        if (this.properties!=null){
            for (PropertyInfo property:this.properties){
                if (property.getValue()!=null && !property.getValue().trim().isEmpty()){
                    properties.setProperty(property.getKey(), property.getValue().trim());
                }
            }
        }
        return properties;
    }

    public PropertyInfo[] getProperties() {
        return properties;
    }

    public void setProperties(PropertyInfo[] properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "ConnectionInfo{name:" + name + " provider:" + driver + " url:" + url +
                " username:" + username + "}";
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
