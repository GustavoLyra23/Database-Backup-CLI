package org.example.entities;

public class ConnectionEntity {
    private String dbType;
    private String url;
    private String user;
    private String password;
    private String dbName;

    public ConnectionEntity() {
    }

    public ConnectionEntity(String dbType, String url, String user, String password, String dbName) {
        this.dbType = dbType;
        this.url = url;
        this.user = user;
        this.password = password;
        this.dbName = dbName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String dbType;
        private String url;
        private String user;
        private String password;
        private String dbName;

        public Builder dbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder dbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public ConnectionEntity build() {
            return new ConnectionEntity(dbType, url, user, password, dbName);
        }
    }
    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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

