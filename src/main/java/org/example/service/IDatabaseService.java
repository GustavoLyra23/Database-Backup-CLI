package org.example.service;

import java.sql.SQLException;

public interface IDatabaseService {

    void connect(String url, String user, String password);

    void disconnect() throws SQLException;

    void listData() throws SQLException;

    String exportData(String tableName)throws SQLException ;


}
