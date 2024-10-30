package org.example.service.impl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.example.entities.DbConnectionEntity;
import org.example.service.DatabaseRestorer;
import org.example.util.EncryptionUtil;
import org.example.util.ProgressBarUtil;

import javax.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.sql.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class SQLRestorer implements DatabaseRestorer {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final SQLRestorer instance = new SQLRestorer();

    private SQLRestorer() {
    }

    public static SQLRestorer getInstance() {
        return instance;
    }

    @Override
    public void restoreDatabase(String key, List<String> saves, String fileDbType, String fileName, DbConnectionEntity dbConnectionEntity) {
        Path backupPath = Paths.get(System.getProperty("user.home"), "backups", fileDbType, fileName);

        if (!Files.isDirectory(backupPath)) {
            System.out.println("Backup directory not found: " + backupPath);
            return;
        }

        try {
            List<Path> fileList = Files.list(backupPath)
                    .filter(file -> {
                        String tableName = extractTableName(file.getFileName().toString());
                        return saves == null || saves.isEmpty() || saves.contains(tableName);
                    })
                    .toList();

            if (fileList.isEmpty()) {
                System.out.println("No matching backup files found.");
                return;
            }

            try (Connection connection = DriverManager.getConnection(dbConnectionEntity.getUrl(),
                    dbConnectionEntity.getUser(), dbConnectionEntity.getPassword())) {
                int totalFiles = fileList.size();
                for (int i = 0; i < totalFiles; i++) {
                    Path filePath = fileList.get(i);
                    if (!processBackupFile(filePath, key, connection)) {
                        System.out.println("Access denied for encrypted file: " + filePath.getFileName());
                        return;
                    }
                    ProgressBarUtil.printProgress(i + 1, totalFiles);
                    Thread.sleep(200 * 3);
                }
                System.out.println("\nRestore completed successfully.");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            System.err.println("Error accessing the directory: " + backupPath);
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
        }
    }

    private boolean processBackupFile(Path filePath, String key, Connection connection) {
        String fileName = filePath.getFileName().toString();
        boolean isEncrypted = fileName.contains("_encrypted");

        if (isEncrypted && key == null) {
            System.out.println("Access denied: Encrypted file requires a key.");
            return false;
        }

        Path extractedFilePath = Paths.get(filePath.toString().replace(".gz", ""));
        try (InputStream fileInputStream = new FileInputStream(filePath.toFile());
             GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
             FileOutputStream extractedFileOutputStream = new FileOutputStream(extractedFilePath.toFile())) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                extractedFileOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            System.err.println("Error decompressing file: " + filePath + " - " + e.getMessage());
            return false;
        }

        try (InputStream finalInputStream = isEncrypted ? getDecryptedInputStream(new FileInputStream(extractedFilePath.toFile()), key)
                : new FileInputStream(extractedFilePath.toFile());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(finalInputStream))) {

            String tableName = extractTableName(fileName);
            restoreTableFromBackup(bufferedReader, tableName, connection);
            return true;

        } catch (Exception e) {
            System.err.println("Error processing file: " + filePath + " - " + e.getMessage());
            return false;
        } finally {
            try {
                Files.deleteIfExists(extractedFilePath);
            } catch (IOException e) {
                System.err.println("Failed to delete temporary extracted file: " + extractedFilePath);
            }
        }
    }

    private InputStream getDecryptedInputStream(InputStream encryptedInputStream, String key) throws Exception {
        SecretKey secretKey = EncryptionUtil.decodeKey(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new CipherInputStream(encryptedInputStream, cipher);
    }

    private String extractTableName(String fileName) {
        int lastUnderscoreIndex = fileName.lastIndexOf("_2");
        //TODO: ensure it works in 3000s
        if (lastUnderscoreIndex != -1) {
            return fileName.substring(0, lastUnderscoreIndex);
        }
        return fileName;
    }

    private void restoreTableFromBackup(BufferedReader bufferedReader, String tableName, Connection connection) throws IOException, SQLException {
        String line;
        boolean schemaProcessed = false;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("-- SCHEMA")) {
                line = bufferedReader.readLine();
                if (line != null && line.startsWith("CREATE TABLE")) {
                    executeCreateTable(line, connection);
                    schemaProcessed = true;
                }
            } else if (schemaProcessed && line.startsWith("-- DATA")) {
                bufferedReader.readLine();
                while ((line = bufferedReader.readLine()) != null) {
                    insertData(line, tableName, connection);
                }
            }
        }
    }


    private void executeCreateTable(String createStatement, Connection connection) throws SQLException {
        String tableName = createStatement.split(" ")[2];
        try (PreparedStatement dropStmt = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
             PreparedStatement createStmt = connection.prepareStatement(createStatement)) {
            dropStmt.executeUpdate();
            createStmt.executeUpdate();
        }
    }

    private void insertData(String line, String tableName, Connection connection) throws SQLException {
        if (line == null || line.isEmpty()) return;

        String[] values = line.split(",");
        String placeholders = String.join(",", java.util.Collections.nCopies(values.length, "?"));
        String query = "INSERT INTO " + tableName + " VALUES (" + placeholders + ")";
        ResultSetMetaData metaData;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName + " LIMIT 1")) {
            metaData = rs.getMetaData();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < values.length; i++) {
                int columnType = metaData.getColumnType(i + 1);
                String value = values[i].trim();

                switch (columnType) {
                    case Types.BIGINT:
                        preparedStatement.setLong(i + 1, Long.parseLong(value));
                        break;
                    case Types.INTEGER:
                        preparedStatement.setInt(i + 1, Integer.parseInt(value));
                        break;
                    case Types.DOUBLE:
                        preparedStatement.setDouble(i + 1, Double.parseDouble(value));
                        break;
                    case Types.FLOAT:
                        preparedStatement.setFloat(i + 1, Float.parseFloat(value));
                        break;
                    case Types.DATE:
                        preparedStatement.setDate(i + 1, Date.valueOf(value));
                        break;
                    default:
                        preparedStatement.setString(i + 1, value);
                        break;
                }
            }
            preparedStatement.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error inserting data into " + tableName + ": " + e.getMessage());
            throw e;
        }
    }
}
