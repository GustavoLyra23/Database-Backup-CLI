package org.example.service.impl

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.example.service.DatabaseExporter
import org.example.util.EncryptionUtil.decodeKey
import org.example.util.EncryptionUtil.validateKey
import org.example.util.ProgressBarUtil.printProgress
import org.example.util.UI.exportingDbMessage
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey

class MongoDatabaseExporter(private val uri: String?, private val dbName: String?) : DatabaseExporter {
    override fun exportDatabase(key: String?, entities: MutableList<String>?) {
        var entities = entities
        exportingDbMessage()
        if (key != null) {
            validateKey(key)
        }
        val timestamp = SimpleDateFormat("yyyy-MMdd_HHmmss").format(Date())
        val backupPath = "$MAIN_BACKUP_FOLDER_PATH/$timestamp"
        val backupDir = File(backupPath)
        if (!backupDir.mkdirs()) {
            println("Error while creating directory: $backupPath")
            return
        }

        try {
            MongoClients.create(uri).use { mongoClient ->
                val database = mongoClient.getDatabase(dbName)
                if (entities == null) {
                    entities = database.listCollectionNames()
                        .into(ArrayList<String?>()) as MutableList<String>?
                }

                var i = 0
                if (entities != null) {
                    for (collectionName in entities) {
                        val collection = database.getCollection(collectionName)
                        val filePath =
                            backupPath + "/" + collectionName + "_" + timestamp + (if (key != null) "_encrypted" else "") + ".json.gz"
                        val secretKey = if (key != null) decodeKey(key) else null
                        exportCollectionToFile(collection, filePath, secretKey)
                        printProgress(i + 1, entities.size)
                        Thread.sleep(1000)
                        i++
                    }
                }
                println("\nBackup completed: $backupPath")
            }
        } catch (e: Exception) {
            System.err.println("Error while connecting to database: " + e.message)
        }
    }

    @Throws(Exception::class)
    private fun exportCollectionToFile(collection: MongoCollection<Document?>, filePath: String, key: SecretKey?) {
        collection.find().iterator().use { cursor ->
            FileOutputStream(filePath).use { fileOutputStream ->
                GZIPOutputStream(fileOutputStream).use { gzipOutputStream ->
                    if (key != null) getEncryptedOutputStream(
                        gzipOutputStream,
                        key
                    ) else gzipOutputStream.use { finalOutputStream ->
                        BufferedWriter(OutputStreamWriter(finalOutputStream)).use { writer ->
                            while (cursor.hasNext()) {
                                val doc = cursor.next()
                                writer.write(doc?.toJson())
                                writer.newLine()
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun getEncryptedOutputStream(outputStream: OutputStream?, key: SecretKey?): OutputStream {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return CipherOutputStream(outputStream, cipher)
    }

    companion object {
        private val MAIN_BACKUP_FOLDER_PATH = System.getProperty("user.home") + "/backups/mongo"
    }
}
