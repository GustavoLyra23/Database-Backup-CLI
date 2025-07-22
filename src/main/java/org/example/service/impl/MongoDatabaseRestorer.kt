package org.example.service.impl

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.example.entities.ConnectionEntity
import org.example.service.DatabaseRestorer
import org.example.util.EncryptionUtil.decodeKey
import org.example.util.ProgressBarUtil.printProgress
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.GZIPInputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

class MongoDatabaseRestorer private constructor() : DatabaseRestorer {
    override fun restoreDatabase(
        key: String?,
        saves: MutableList<String?>?,
        fileDbType: String?,
        fileName: String?,
        connectionEntity: ConnectionEntity?
    ) {
        if (fileDbType == null || fileName == null || connectionEntity == null) {
            println("Invalid parameters provided for restore operation")
            return
        }

        val backupPath = Paths.get(System.getProperty("user.home"), "backups", fileDbType, fileName)

        if (!Files.isDirectory(backupPath)) {
            println("Backup directory not found: $backupPath")
            return
        }

        try {
            MongoClients.create(connectionEntity.url).use { mongoClient ->
                val database = mongoClient.getDatabase(connectionEntity.dbName)
                val fileList = Files.list(backupPath).filter { file ->
                        val collectionName = extractCollectionName(file.fileName.toString())
                        saves.isNullOrEmpty() || saves.contains(collectionName)
                    }.toList()

                if (fileList.isEmpty()) {
                    println("No matching backup files found.")
                    return
                }

                fileList.forEachIndexed { index, filePath ->
                    if (!restoreCollectionFromFile(filePath, key, database)) {
                        println("Access denied for encrypted file: ${filePath.fileName}")
                        return
                    }
                    printProgress(index + 1, fileList.size)
                }
                println("\nRestore completed successfully.")
            }
        } catch (e: Exception) {
            System.err.println("Error restoring MongoDB database: ${e.message}")
        }
    }

    private fun restoreCollectionFromFile(filePath: Path, key: String?, database: MongoDatabase): Boolean {
        val fileName = filePath.fileName.toString()
        val isEncrypted = "_encrypted" in fileName

        if (isEncrypted && key == null) {
            println("Access denied: Encrypted file requires a key.")
            return false
        }

        try {
            FileInputStream(filePath.toFile()).use { fileInputStream ->
                val inputStream = if (isEncrypted) {
                    getDecryptedInputStream(fileInputStream, key)
                } else {
                    GZIPInputStream(fileInputStream)
                }

                inputStream.use { finalInputStream ->
                    BufferedReader(InputStreamReader(finalInputStream)).use { reader ->
                        val collectionName = extractCollectionName(fileName)
                        val collection = database.getCollection(collectionName)
                        collection.drop()

                        reader.forEachLine { line ->
                            if (line.isNotBlank()) {
                                collection.insertOne(Document.parse(line))
                            }
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            System.err.println("Error processing file: $filePath - ${e.message}")
            return false
        }
    }

    @Throws(Exception::class)
    private fun getDecryptedInputStream(encryptedInputStream: InputStream, key: String?): InputStream {
        val secretKey = decodeKey(key)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return CipherInputStream(GZIPInputStream(encryptedInputStream), cipher)
    }

    private fun extractCollectionName(fileName: String): String {
        val lastUnderscoreIndex = fileName.lastIndexOf("_2")
        return if (lastUnderscoreIndex != -1) {
            fileName.substring(0, lastUnderscoreIndex)
        } else {
            fileName
        }
    }

    companion object {
        val instance: MongoDatabaseRestorer = MongoDatabaseRestorer()
    }
}