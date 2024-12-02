package org.example.entities.builders;

import org.example.entities.DbConnectionEntity;

public class DbBuilder {

    private String dbType;
    private String url;
    private String user;
    private String password;
    private String dbName;

    public DbBuilder dbType(String dbType) {
        this.dbType = dbType;
        return this;
    }

    public DbBuilder url(String jdbcUrl) {
        this.url = jdbcUrl;
        return this;
    }

    public DbBuilder user(String user) {
        this.user = user;
        return this;
    }

    public DbBuilder password(String password) {
        this.password = password;
        return this;
    }

    public DbBuilder DbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public DbConnectionEntity build() {
        DbConnectionEntity dbConnectionEntity = new DbConnectionEntity();
        dbConnectionEntity.setDbType(dbType);
        dbConnectionEntity.setUrl(url);
        dbConnectionEntity.setUser(user);
        dbConnectionEntity.setPassword(password);
        dbConnectionEntity.setDbName(dbName);
        return dbConnectionEntity;
    }


}
