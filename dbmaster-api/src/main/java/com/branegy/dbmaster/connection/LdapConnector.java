package com.branegy.dbmaster.connection;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.branegy.service.connection.model.DatabaseConnection;
import com.branegy.service.connection.model.DatabaseConnection.PropertyInfo;

// for security http\://docs.oracle.com/javase/jndi/tutorial/ldap/security/sasl.html
// http://docs.oracle.com/javase/jndi/tutorial/ldap/security/src/Mutual.java
public class LdapConnector extends Connector {

    public LdapConnector(ConnectorInfo driverInfo, DatabaseConnection ci) {
        super(driverInfo, ci);
    }

    @Override
    public Dialect connect() {
        Hashtable<String,String> environment = new Hashtable<String, String>();
        
        environment.put(Context.PROVIDER_URL, ci.getUrl());

        if (ci.getUsername()!=null && ci.getUsername().length()>0) {
            environment.put(Context.SECURITY_PRINCIPAL,   ci.getUsername());
            environment.put(Context.SECURITY_CREDENTIALS, ci.getPassword());
        }
        
        for (PropertyInfo property : ci.getProperties()) {
            if (property.getValue()!=null && !property.getValue().trim().isEmpty()){
                environment.put(property.getKey(), property.getValue().trim());
            }
        }
        
        try {
            final DirContext context = new InitialDirContext(environment);
            return new LdapDialect(context);
        } catch (Exception e) {
            throw new ConnectionApiException("Cannot connect to "+ci.getName()+"("+e.getMessage()+")", e);
        }
    }
    
    public final class LdapDialect implements Dialect {
        private final DirContext context;

        private LdapDialect(DirContext context) {
            this.context = context;
        }
        
        public DirContext getContext() {
            return context;
        }

        @Override
        public void close() {
            try {
                context.close();
            } catch (Exception e) {
                throw new ConnectionApiException(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean testConnection() {
        try {
            Dialect dialect = connect();
            dialect.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // How to check supported authentication supportedSASLMechanisms from root DSE
    // DirContext ctx = new InitialDirContext();
    // def params = ["supportedSASLMechanisms"] as String[]
    // Attributes attrs = ctx.getAttributes(url, params);
    // println attrs
    // Close the context when we're done
    // ctx.close();

}