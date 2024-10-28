package org.example.entities;

import org.example.builders.DbBuilder;

public class DbConnectionEntity {

    private String dbType;
    private String url;
    private String user;
    private String password;


    public static DbBuilder builder() {
        return new DbBuilder();
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String jdbcUrl) {
        this.url = jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
