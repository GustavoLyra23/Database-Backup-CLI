package org.example.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.example.entities.ConnectionEntity
import org.example.service.DatabaseRestorer
import org.example.util.EncryptionUtil.decodeKey
import org.example.util.ProgressBarUtil.printProgress
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.Security
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Types
import java.util.zip.GZIPInputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

class SQLRestorer private constructor() : DatabaseRestorer {
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
            println("Backup directory not found❗: $backupPath")
            return
        }

        try {
            val fileList = Files.list(backupPath)
                .filter { file ->
                    val tableName = extractTableName(file.fileName.toString())
                    saves.isNullOrEmpty() || saves.contains(tableName)
                }
                .toList()

            if (fileList.isEmpty()) {
                println("No matching backup files found.")
                return
            }

            DriverManager.getConnection(
                connectionEntity.url,
                connectionEntity.user,
                connectionEntity.password
            ).use { connection ->
                val totalFiles = fileList.size
                for (i in fileList.indices) {
                    val filePath = fileList[i]
                    if (!processBackupFile(filePath, key, connection)) {
                        println("Access denied for encrypted file: ${filePath.fileName}")
                        return
                    }
                    printProgress(i + 1, totalFiles)
                    Thread.sleep(600)
                }
                println("\nRestore completed successfully.")
            }
        } catch (e: IOException) {
            System.err.println("Error accessing the directory: $backupPath")
        } catch (e: SQLException) {
            System.err.println("Error establishing database connection: ${e.message}")
        }
    }

    private fun processBackupFile(filePath: Path, key: String?, connection: Connection): Boolean {
        val fileName = filePath.fileName.toString()
        val isEncrypted = "_encrypted" in fileName

        if (isEncrypted && key == null) {
            println("Access denied: Encrypted file requires a key.")
            return false
        }

        val extractedFilePath = Paths.get(filePath.toString().replace(".gz", ""))

        // Decompress file
        try {
            FileInputStream(filePath.toFile()).use { fileInputStream ->
                GZIPInputStream(fileInputStream).use { gzipInputStream ->
                    FileOutputStream(extractedFilePath.toFile()).use { extractedFileOutputStream ->
                        gzipInputStream.copyTo(extractedFileOutputStream)
                    }
                }
            }
        } catch (e: IOException) {
            System.err.println("Error decompressing file: $filePath - ${e.message}")
            return false
        }

        // Process decompressed file
        try {
            val inputStream = if (isEncrypted) {
                getDecryptedInputStream(FileInputStream(extractedFilePath.toFile()), key)
            } else {
                FileInputStream(extractedFilePath.toFile())
            }

            inputStream.use { finalInputStream ->
                BufferedReader(InputStreamReader(finalInputStream)).use { bufferedReader ->
                    val tableName = extractTableName(fileName)
                    restoreTableFromBackup(bufferedReader, tableName, connection)
                }
            }
            return true
        } catch (e: Exception) {
            System.err.println("Error processing file: $filePath - ${e.message}")
            return false
        } finally {
            try {
                Files.deleteIfExists(extractedFilePath)
            } catch (e: IOException) {
                System.err.println("Failed to delete temporary extracted file: $extractedFilePath")
            }
        }
    }

    @Throws(Exception::class)
    private fun getDecryptedInputStream(encryptedInputStream: InputStream, key: String?): InputStream {
        val secretKey = decodeKey(key)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return CipherInputStream(encryptedInputStream, cipher)
    }

    private fun extractTableName(fileName: String): String {
        val lastUnderscoreIndex = fileName.lastIndexOf("_2")
        return if (lastUnderscoreIndex != -1) {
            fileName.substring(0, lastUnderscoreIndex)
        } else {
            fileName
        }
    }

    @Throws(IOException::class, SQLException::class)
    private fun restoreTableFromBackup(bufferedReader: BufferedReader, tableName: String, connection: Connection) {
        var schemaProcessed = false

        bufferedReader.forEachLine { line ->
            when {
                line.startsWith("-- SCHEMA") -> {
                    val nextLine = bufferedReader.readLine()
                    if (nextLine?.startsWith("CREATE TABLE") == true) {
                        executeCreateTable(nextLine, connection)
                        schemaProcessed = true
                    }
                }

                schemaProcessed && line.startsWith("-- DATA") -> {
                    bufferedReader.readLine() // Skip header line
                    bufferedReader.forEachLine { dataLine ->
                        insertData(dataLine, tableName, connection)
                    }
                }
            }
        }
    }

    @Throws(SQLException::class)
    private fun executeCreateTable(createStatement: String, connection: Connection) {
        val tableNameFromStatement = createStatement.split(" ")[2]

        connection.prepareStatement("DROP TABLE IF EXISTS $tableNameFromStatement").use { dropStmt ->
            connection.prepareStatement(createStatement).use { createStmt ->
                dropStmt.executeUpdate()
                createStmt.executeUpdate()
            }
        }
    }

    @Throws(SQLException::class)
    private fun insertData(line: String, tableName: String, connection: Connection) {
        if (line.isBlank()) return

        val values = line.split(",").map { it.trim() }.toTypedArray()
        val placeholders = values.joinToString(",") { "?" }
        val insertQuery = "INSERT INTO $tableName VALUES ($placeholders)"

        val metaData = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT * FROM $tableName LIMIT 1").use { rs ->
                rs.metaData
            }
        }

        try {
            connection.prepareStatement(insertQuery).use { preparedStatement ->
                values.forEachIndexed { index, value ->
                    val columnType = metaData.getColumnType(index + 1)
                    val trimmedValue = value.trim()

                    when (columnType) {
                        Types.BIGINT -> preparedStatement.setLong(index + 1, trimmedValue.toLong())
                        Types.INTEGER -> preparedStatement.setInt(index + 1, trimmedValue.toInt())
                        Types.DOUBLE -> preparedStatement.setDouble(index + 1, trimmedValue.toDouble())
                        Types.FLOAT -> preparedStatement.setFloat(index + 1, trimmedValue.toFloat())
                        Types.DATE -> preparedStatement.setDate(index + 1, java.sql.Date.valueOf(trimmedValue))
                        else -> preparedStatement.setString(index + 1, trimmedValue)
                    }
                }
                preparedStatement.executeUpdate()
            }
        } catch (e: SQLException) {
            System.err.println("Error inserting data into $tableName: ${e.message}")
            throw e
        } catch (e: NumberFormatException) {
            System.err.println("Error parsing data for $tableName: ${e.message}")
            throw e
        }
    }

    companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }

        val instance: SQLRestorer = SQLRestorer()
    }
}