package org.example.service.impl

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.example.service.DatabaseExporter
import org.example.util.EncryptionUtil.decodeKey
import org.example.util.EncryptionUtil.validateKey
import org.example.util.ProgressBarUtil.printProgress
import org.example.util.UI.exportingDbMessage
import java.io.*
import java.security.Security
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey

class SqlDatabaseExporter(jdbcUrl: String?, user: String?, password: String?) : DatabaseExporter {
    private val jdbcUrl: String?
    private val user: String?
    private val password: String?

    init {
        Security.addProvider(BouncyCastleProvider())
        this.jdbcUrl = jdbcUrl
        this.user = user
        this.password = password
    }

    override fun exportDatabase(key: String?, entities: MutableList<String?>?) {
        exportingDbMessage()
        if (key != null) {
            validateKey(key)
        }
        val timestamp = SimpleDateFormat("yyyy-MMdd_HHmmss").format(Date())
        val currentBackupPath: String = "$MAIN_BACKUP_FOLDER_PATH/$timestamp"
        val backupDir = File(currentBackupPath)
        if (!backupDir.mkdirs()) {
            println("Error while creating file❗: $currentBackupPath")
            return
        }

        var success = false
        try {
            DriverManager.getConnection(jdbcUrl, user, password).use { connection ->
                connection.autoCommit = false
                val tables = if (entities == null || entities.isEmpty()) getTables(connection) else entities
                val totalTables = tables.size
                for (i in 0..<totalTables) {
                    val table = tables.get(i)
                    val tableBackupFilePath =
                        currentBackupPath + "/" + table + "_" + timestamp + (if (key != null) "_encrypted" else "") + ".csv.gz"
                    val secretKey = if (key != null) decodeKey(key) else null
                    exportTableToFile(connection, table, tableBackupFilePath, secretKey)
                    //TODO: I should execute this fancy animation on a separte thread to avoid blocking the main thread...
                    printProgress(i + 1, totalTables)
                    Thread.sleep((200 * 4).toLong())
                }

                success = true
                connection.commit()
                println("\nBackup completed: $currentBackupPath")
            }
        } catch (e: Exception) {
            System.err.println("Error while exporting the database❗...")
            deleteDirectory(backupDir)
        } finally {
            if (!success) {
                deleteDirectory(backupDir)
            }
        }
    }


    @Throws(SQLException::class)
    private fun getTables(connection: Connection): MutableList<String?> {
        val tables: MutableList<String?> = ArrayList<String?>()
        val metaData = connection.metaData
        metaData.getTables(null, null, "%", arrayOf<String>("TABLE")).use { rs ->
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"))
            }
        }
        return tables
    }

    @Throws(Exception::class)
    private fun exportTableToFile(connection: Connection, tableName: String?, filePath: String, key: SecretKey?) {
        connection.prepareStatement("SELECT * FROM $tableName").use { statement ->
            statement.executeQuery().use { resultSet ->
                FileOutputStream(filePath).use { fileOutputStream ->
                    BufferedOutputStream(fileOutputStream, 8192).use { bufferedOutputStream ->
                        if (key != null) getEncryptedOutputStream(bufferedOutputStream, key) else GZIPOutputStream(
                            bufferedOutputStream
                        ).use { finalOutputStream ->
                            BufferedWriter(OutputStreamWriter(finalOutputStream), 8192).use { writer ->
                                writeTableSchema(connection, tableName, writer)
                                writeColumnNames(resultSet, writer)
                                writeTableData(resultSet, writer)
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(SQLException::class, IOException::class)
    private fun writeTableSchema(connection: Connection, tableName: String?, writer: BufferedWriter) {
        val metaData = connection.metaData
        metaData.getColumns(null, null, tableName, null).use { columns ->
            val createStatement = StringBuilder("CREATE TABLE ").append(tableName).append(" (")
            var first = true
            while (columns.next()) {
                if (!first) {
                    createStatement.append(", ")
                }
                val columnName = columns.getString("COLUMN_NAME")
                val columnType = columns.getString("TYPE_NAME")
                val columnSize = columns.getInt("COLUMN_SIZE")
                createStatement.append(columnName).append(" ").append(columnType)
                if (columnType.equals("VARCHAR", ignoreCase = true) || columnType.equals("CHAR", ignoreCase = true)) {
                    createStatement.append("(").append(columnSize).append(")")
                }
                first = false
            }
            createStatement.append(");")
            writer.write("-- SCHEMA\n")
            writer.write(createStatement.toString())
            writer.newLine()
            writer.write("-- DATA")
            writer.newLine()
        }
    }

    @Throws(SQLException::class, IOException::class)
    private fun writeColumnNames(resultSet: ResultSet, writer: BufferedWriter) {
        val columnCount = resultSet.metaData.columnCount
        val header = StringBuilder()
        for (i in 1..columnCount) {
            header.append(resultSet.metaData.getColumnName(i))
            if (i < columnCount) header.append(",")
        }
        writer.write(header.toString())
        writer.newLine()
    }

    @Throws(SQLException::class, IOException::class)
    private fun writeTableData(resultSet: ResultSet, writer: BufferedWriter) {
        val columnCount = resultSet.metaData.getColumnCount()
        val row = StringBuilder()
        while (resultSet.next()) {
            row.setLength(0)
            for (i in 1..columnCount) {
                row.append(resultSet.getString(i))
                if (i < columnCount) row.append(",")
            }
            writer.write(row.toString())
            writer.newLine()
        }
    }

    @Throws(Exception::class)
    private fun getEncryptedOutputStream(fileOutputStream: OutputStream, key: SecretKey?): OutputStream {
        val gzipOutputStream = GZIPOutputStream(fileOutputStream, 8192)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return CipherOutputStream(gzipOutputStream, cipher)
    }


    private fun deleteDirectory(directory: File) {
        if (directory.isDirectory()) {
            for (file in Objects.requireNonNull(directory.listFiles())) {
                deleteDirectory(file)
            }
        }
        if (!directory.delete()) {
            System.err.println("Failed to delete❗: " + directory.absolutePath)
        }
    }

    companion object {
        private val MAIN_BACKUP_FOLDER_PATH = System.getProperty("user.home") + "/backups/sql"
    }
}
