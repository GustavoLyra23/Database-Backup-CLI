package org.example.service.impl;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.entities.ConnectionEntity;
import org.example.service.DatabaseRestorer;
import org.example.util.EncryptionUtil;
import org.example.util.ProgressBarUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MongoDatabaseRestorer implements DatabaseRestorer {

    private static final MongoDatabaseRestorer instance = new MongoDatabaseRestorer();

    private MongoDatabaseRestorer() {
    }

    public static MongoDatabaseRestorer getInstance() {
        return instance;
    }

    @Override
    public void restoreDatabase(String key, List<String> collections, String fileDbType, String fileName, ConnectionEntity connectionEntity) {
        Path backupPath = Paths.get(System.getProperty("user.home"), "backups", fileDbType, fileName);

        if (!Files.isDirectory(backupPath)) {
            System.out.println("Backup directory not found: " + backupPath);
            return;
        }

        try (var mongoClient = MongoClients.create(connectionEntity.getUrl())) {
            MongoDatabase database = mongoClient.getDatabase(connectionEntity.getDbName());

            List<Path> fileList = Files.list(backupPath)
                    .filter(file -> {
                        String collectionName = extractCollectionName(file.getFileName().toString());
                        return collections == null || collections.isEmpty() || collections.contains(collectionName);
                    })
                    .toList();

            int i = 0;
            for (Path filePath : fileList) {
                if (!restoreCollectionFromFile(filePath, key, database)) {
                    System.out.println("Access denied for encrypted file: " + filePath.getFileName());
                    return;
                }
                ProgressBarUtil.printProgress(++i, fileList.size());
            }
            System.out.println("\nRestore completed successfully.");
        } catch (Exception e) {
            System.err.println("Error restoring MongoDB database: " + e.getMessage());
        }
    }

    private boolean restoreCollectionFromFile(Path filePath, String key, MongoDatabase database) {
        String fileName = filePath.getFileName().toString();
        boolean isEncrypted = fileName.contains("_encrypted");

        if (isEncrypted && key == null) {
            System.out.println("Access denied: Encrypted file requires a key.");
            return false;
        }

        try (InputStream fileInputStream = new FileInputStream(filePath.toFile());
             InputStream finalInputStream = isEncrypted ? getDecryptedInputStream(fileInputStream, key) : new GZIPInputStream(fileInputStream);
             BufferedReader reader = new BufferedReader(new InputStreamReader(finalInputStream))) {

            String collectionName = extractCollectionName(fileName);
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.drop();

            String line;
            while ((line = reader.readLine()) != null) {
                collection.insertOne(Document.parse(line));
            }
            return true;

        } catch (Exception e) {
            System.err.println("Error processing file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }

    private InputStream getDecryptedInputStream(InputStream encryptedInputStream, String key) throws Exception {
        SecretKey secretKey = EncryptionUtil.decodeKey(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new CipherInputStream(encryptedInputStream, cipher);
    }

    private String extractCollectionName(String fileName) {
        int lastUnderscoreIndex = fileName.lastIndexOf("_2");
        if (lastUnderscoreIndex != -1) {
            return fileName.substring(0, lastUnderscoreIndex);
        }
        return fileName;
    }
}
