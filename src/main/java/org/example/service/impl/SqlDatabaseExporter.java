package org.example.service.impl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.example.service.DatabaseExporter;
import org.example.util.EncryptionUtil;
import org.example.util.ProgressBarUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.Security;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import static org.example.util.UI.exportingDbMessage;

public class SqlDatabaseExporter implements DatabaseExporter {

    private final String jdbcUrl;
    private final String user;
    private final String password;

    private static final String MAIN_BACKUP_FOLDER_PATH = System.getProperty("user.home") + "/backups/sql";

    public SqlDatabaseExporter(String jdbcUrl, String user, String password) {
        Security.addProvider(new BouncyCastleProvider());
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    @Override
    public void exportDatabase(String key, List<String> entities) {
        exportingDbMessage();
        if (key != null) {
            EncryptionUtil.validateKey(key);
        }
        String timestamp = new SimpleDateFormat("yyyy-MMdd_HHmmss").format(new Date());
        String currentBackupPath = MAIN_BACKUP_FOLDER_PATH + "/" + timestamp;
        File backupDir = new File(currentBackupPath);
        if (!backupDir.mkdirs()) {
            System.out.println("Error while creating file❗: " + currentBackupPath);
            return;
        }

        boolean success = false;
        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password)) {
            connection.setAutoCommit(false);
            List<String> tables = (entities == null || entities.isEmpty()) ? getTables(connection) : entities;
            int totalTables = tables.size();
            for (int i = 0; i < totalTables; i++) {
                String table = tables.get(i);
                String tableBackupFilePath = currentBackupPath + "/" + table + "_" + timestamp + (key != null ? "_encrypted" : "") + ".csv.gz";
                SecretKey secretKey = key != null ? EncryptionUtil.decodeKey(key) : null;
                exportTableToFile(connection, table, tableBackupFilePath, secretKey);
                //TODO: I should execute this fancy animation on a separte thread to avoid blocking the main thread...
                ProgressBarUtil.printProgress(i + 1, totalTables);
                Thread.sleep(200 * 4);
            }

            success = true;
            connection.commit();
            System.out.println("\nBackup completed: " + currentBackupPath);

        } catch (Exception e) {
            System.err.println("Error while exporting the database❗...");
            deleteDirectory(backupDir);
        } finally {
            if (!success) {
                deleteDirectory(backupDir);
            }
        }
    }


    private List<String> getTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    private void exportTableToFile(Connection connection, String tableName, String filePath, SecretKey key) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName);
             ResultSet resultSet = statement.executeQuery();
             FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 8192);
             OutputStream finalOutputStream = (key != null) ? getEncryptedOutputStream(bufferedOutputStream, key) : new GZIPOutputStream(bufferedOutputStream);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(finalOutputStream), 8192)) {

            writeTableSchema(connection, tableName, writer);
            writeColumnNames(resultSet, writer);
            writeTableData(resultSet, writer);
        }
    }

    private void writeTableSchema(Connection connection, String tableName, BufferedWriter writer) throws SQLException, IOException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            StringBuilder createStatement = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
            boolean first = true;
            while (columns.next()) {
                if (!first) {
                    createStatement.append(", ");
                }
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                createStatement.append(columnName).append(" ").append(columnType);
                if (columnType.equalsIgnoreCase("VARCHAR") || columnType.equalsIgnoreCase("CHAR")) {
                    createStatement.append("(").append(columnSize).append(")");
                }
                first = false;
            }
            createStatement.append(");");
            writer.write("-- SCHEMA\n");
            writer.write(createStatement.toString());
            writer.newLine();
            writer.write("-- DATA");
            writer.newLine();
        }
    }

    private void writeColumnNames(ResultSet resultSet, BufferedWriter writer) throws SQLException, IOException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            header.append(resultSet.getMetaData().getColumnName(i));
            if (i < columnCount) header.append(",");
        }
        writer.write(header.toString());
        writer.newLine();
    }

    private void writeTableData(ResultSet resultSet, BufferedWriter writer) throws SQLException, IOException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        StringBuilder row = new StringBuilder();
        while (resultSet.next()) {
            row.setLength(0);
            for (int i = 1; i <= columnCount; i++) {
                row.append(resultSet.getString(i));
                if (i < columnCount) row.append(",");
            }
            writer.write(row.toString());
            writer.newLine();
        }
    }

    private OutputStream getEncryptedOutputStream(OutputStream fileOutputStream, SecretKey key) throws Exception {
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream, 8192);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new CipherOutputStream(gzipOutputStream, cipher);
    }



    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                deleteDirectory(file);
            }
        }
        if (!directory.delete()) {
            System.err.println("Failed to delete❗: " + directory.getAbsolutePath());
        }
    }
}
