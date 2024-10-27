package org.example.service.impl;

import org.example.service.DatabaseExporter;
import org.example.util.ProgressBarUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

public class SqlDatabaseExporter implements DatabaseExporter {

    private final String jdbcUrl;
    private final String user;
    private final String password;


    private static final String MAIN_BACKUP_FOLDER_PATH = System.getProperty("user.home") + "/backups";

    public SqlDatabaseExporter(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    @Override
    public void exportDatabase(String key) throws Exception {
        if (key != null && !EncryptionService.getInstance().isValidAESKey(key)) {
            throw new IllegalArgumentException("A chave fornecida não é uma chave AES válida.");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String currentBackupPath = MAIN_BACKUP_FOLDER_PATH + "/" + timestamp;
        File backupDir = new File(currentBackupPath);

        if (!backupDir.mkdirs()) {
            throw new IOException("Falha ao criar a pasta de backup em: " + currentBackupPath);
        }

        boolean success = false;

        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password)) {
            connection.setAutoCommit(false);

            List<String> tables = getTables(connection);
            int totalTables = tables.size();

            for (int i = 0; i < totalTables; i++) {
                String table = tables.get(i);
                String tableBackupFilePath = currentBackupPath + "/" + table + "_" + timestamp + (key != null ? "_encrypted" : "") + ".csv.gz";
                SecretKey secretKey = key != null ? EncryptionService.getInstance().decodeKey(key) : null;
                exportTableToFile(connection, table, tableBackupFilePath, secretKey);

                ProgressBarUtil.printProgress(i + 1, totalTables);

                Thread.sleep(200 * 4);
            }

            success = true;
            connection.commit();
            System.out.println("\nBackup completo. Local: " + currentBackupPath);

        } catch (Exception e) {
            System.err.println("Erro ao fazer o backup, revertendo as operações...");
            deleteDirectory(backupDir);
            throw e;

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
            writeColumnNames(resultSet, writer);
            writeTableData(resultSet, writer);
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
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new CipherOutputStream(gzipOutputStream, cipher);
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
}
