package com.branegy.dbmaster.core;

import static com.branegy.dbmaster.core.User.QUERY_USER_ALL;
import static com.branegy.dbmaster.core.User.QUERY_USER_BY_API_KEY;
import static com.branegy.dbmaster.core.User.QUERY_USER_BY_LOGIN_PASSWORD;
import static com.branegy.dbmaster.core.User.QUERY_USER_BY_REMEMBER_ID;
import static com.branegy.dbmaster.core.User.QUERY_USER_BY_USER_NAME;
import static com.branegy.dbmaster.core.User.QUERY_USER_COUNT_ACTIVE_USER;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.branegy.persistence.BaseEntity;

@Entity
@Access(AccessType.FIELD)
@NamedQueries({
    @NamedQuery(name = QUERY_USER_ALL, query = "from User u"),
    @NamedQuery(name = QUERY_USER_BY_USER_NAME,query = "from User u where u.userName=:userName"),
    @NamedQuery(name = QUERY_USER_BY_LOGIN_PASSWORD, query = "from User u where " +
            "u.userId=:login and u.password=:password"),
    @NamedQuery(name = QUERY_USER_BY_REMEMBER_ID, query = "from User u where " +
            "u.rememberId=lower(:rememberId) and u.disabled = false"),
    @NamedQuery(name = QUERY_USER_COUNT_ACTIVE_USER, query =
            "select count(u.id) from User u where u.disabled = false"),
    @NamedQuery(name = QUERY_USER_BY_API_KEY, query = "from User u where u.apiKey = :apiKey"),
})
@Table(name="core_user", uniqueConstraints={
    @UniqueConstraint(columnNames={"USER_ID","OPENID_ALIAS","OPENID_URL"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseEntity {
    public static final String QUERY_USER_COUNT_ACTIVE_USER = "User.countActiveUser";
    public static final String QUERY_USER_BY_REMEMBER_ID = "User.findByRememberId";
    public static final String QUERY_USER_BY_LOGIN_PASSWORD = "User.findByLoginPassword";
    public static final String QUERY_USER_BY_API_KEY = "User.findByApiKey";
    public static final String QUERY_USER_BY_USER_NAME = "User.findByUserName";
    public static final String QUERY_USER_ALL = "User.findAll";

    // raw unique user id
    @Column(name="user_id",nullable = false, updatable = false, length = 256)
    @NotNull
    @Size(max = 256)
    private String userId;

    // nice unique user id (use on value for create/update)
    @Column(name="user_name",nullable = false, updatable = false, length = 32, unique = true)
    @NotNull
    @Size(min = 1, max = 32)
    @Pattern(regexp = "([\\S]*|[\\S]+.*[\\S]+)", message = "Value should not start or end with a space")
    private String userName;

    // typically 32 chars
    @Column(name="password",length=255)
    @Size(max = 255)
    private String password;
    
    @Column(name="api_key",length=64)
    @Size(max = 64)
    private String apiKey;

    @Column(name="admin")
    private boolean admin;

    @Column(name="createProject")
    private boolean createProject;

    @Column(name="disabled")
    private boolean disabled;

    @Column(name="first_name",length = 128)
    @Pattern(regexp = "([\\S]*|[\\S]+.*[\\S]+)", message = "Value should not start or end with a space")
    @Size(max = 128)
    private String firstName;

    @Column(name="last_name",length = 128)
    @Pattern(regexp = "([\\S]*|[\\S]+.*[\\S]+)", message = "Value should not start or end with a space")
    @Size(max = 128)
    private String lastName;

    @Column(name="oauth_raw",length = 4096)
    @Size(max = 4096)
    private String oauthRaw;

    @Column(name="openid_alias",length=64,updatable = false)
    @Size(max = 64)
    private String openIdAlias;

    @Column(name="openid_url",length=2048,updatable = false)
    @Size(max = 2048)
    private String openIdUrl;

    // typically 32 chars
    // @see com.branegy.gwt.service.impl.session.SessionManager
    @Column(name="remember_id",length=64)
    @Size(max = 64)
    private String rememberId;

    @Column(name="email",length=255)
    @Size(max = 255)
    private String email;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_login")
    //@Past or equls
    private Date lastLogin;

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isCreateProject() {
        return createProject;
    }

    public void setCreateProject(boolean createProject) {
        this.createProject = createProject;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOpenIdAlias() {
        return openIdAlias;
    }

    public void setOpenIdAlias(String openIdAlias) {
        this.openIdAlias = openIdAlias;
    }

    public String getOpenIdUrl() {
        return openIdUrl;
    }

    public void setOpenIdUrl(String openIdUrl) {
        this.openIdUrl = openIdUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOauthRaw() {
        return oauthRaw;
    }

    public void setOauthRaw(String oauthRaw) {
        this.oauthRaw = oauthRaw;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRememberId() {
        return rememberId;
    }

    public void setRememberId(String rememberId) {
        this.rememberId = rememberId;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
