package org.example.service.impl;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.service.DatabaseExporter;
import org.example.util.EncryptionUtil;
import org.example.util.ProgressBarUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.example.util.UI.exportingDbMessage;

public class MongoDatabaseExporter implements DatabaseExporter {

    private final String uri;
    private final String dbName;
    private static final String MAIN_BACKUP_FOLDER_PATH = System.getProperty("user.home") + "/backups/mongo";

    public MongoDatabaseExporter(String uri, String dbName) {
        this.uri = uri;
        this.dbName = dbName;
    }

    @Override
    public void exportDatabase(String key, List<String> entities) {
        exportingDbMessage();
        if (key != null) {
            EncryptionUtil.validateKey(key);
        }
        String timestamp = new SimpleDateFormat("yyyy-MMdd_HHmmss").format(new Date());
        String backupPath = MAIN_BACKUP_FOLDER_PATH + "/" + timestamp;
        File backupDir = new File(backupPath);
        if (!backupDir.mkdirs()) {
            System.out.println("Error while creating directory: " + backupPath);
            return;
        }

        try (var mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            if (entities == null) {
                entities = database.listCollectionNames().into(new ArrayList<>());
            }

            int i = 0;
            for (String collectionName : entities) {
                MongoCollection<Document> collection = database.getCollection(collectionName);
                String filePath = backupPath + "/" + collectionName + "_" + timestamp + (key != null ? "_encrypted" : "") + ".json.gz";
                SecretKey secretKey = key != null ? EncryptionUtil.decodeKey(key) : null;
                exportCollectionToFile(collection, filePath, secretKey);
                ProgressBarUtil.printProgress(i + 1, entities.size());
                Thread.sleep(1000);
                i++;
            }
            System.out.println("\nBackup completed: " + backupPath);
        } catch (Exception e) {
            System.err.println("Error while connecting to database: " + e.getMessage());
        }
    }

    private void exportCollectionToFile(MongoCollection<Document> collection, String filePath, SecretKey key) throws Exception {
        try (MongoCursor<Document> cursor = collection.find().iterator();
             FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
             OutputStream finalOutputStream = (key != null) ? getEncryptedOutputStream(gzipOutputStream, key) : gzipOutputStream;
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(finalOutputStream))) {

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                writer.write(doc.toJson());
                writer.newLine();
            }
        }
    }

    private OutputStream getEncryptedOutputStream(OutputStream outputStream, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new CipherOutputStream(outputStream, cipher);
    }
}
