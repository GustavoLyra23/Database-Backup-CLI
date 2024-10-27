package org.example.service;

import java.sql.*;

public class SQLDatabaseService implements IDatabaseService {

    private static final SQLDatabaseService instance = new SQLDatabaseService();
    private static Connection connection;

    private SQLDatabaseService() {
    }

    public static SQLDatabaseService getInstance() {
        return instance;
    }

    public void connect(String url, String user, String password) {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error connecting to Database.");
            return;
        }
        System.out.println("Connected to Database.");
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexão com o banco de dados fechada.");
        }
    }

    public void listData() throws SQLException {
        String query = "SHOW TABLES";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Tabelas no banco de dados:");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }

    public String exportData(String tableName) throws SQLException {
        StringBuilder data = new StringBuilder();
        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int columnCount = rs.getMetaData().getColumnCount();

            // Construir cabeçalhos das colunas
            for (int i = 1; i <= columnCount; i++) {
                data.append(rs.getMetaData().getColumnName(i)).append("\t");
            }
            data.append("\n");

            // Adicionar dados das linhas
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    data.append(rs.getString(i)).append("\t");
                }
                data.append("\n");
            }
        }
        return data.toString();
    }
}
