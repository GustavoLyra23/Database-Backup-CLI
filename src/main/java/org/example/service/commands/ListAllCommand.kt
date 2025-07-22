package org.example.service.commands

import org.example.service.Command
import org.example.util.RegexUtil
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class ListAllCommand : Command {
    @Synchronized
    override fun execute(command: String?) {
        val backupsPath = Path.of(System.getProperty("user.home") + "/backups")
        try {
            val dbType = RegexUtil.getDbType(command)
            val folderName = RegexUtil.getFolderName(command)

            if (dbType == null) {
                listRootDirectories(backupsPath)
            } else {
                listDatabaseTypeDirectories(backupsPath, dbType, folderName)
            }
        } catch (e: IOException) {
            println("Error while listing backups❗: " + e.message)
        }
    }

    @Throws(IOException::class)
    private fun listRootDirectories(backupsPath: Path) {
        Files.list(backupsPath)
            .filter { path: Path? -> Files.isDirectory(path) }
            .map<Path?> { obj: Path? -> obj!!.fileName }
            .forEach { x: Path? -> println(x) }
    }

    @Throws(IOException::class)
    private fun listDatabaseTypeDirectories(backupsPath: Path, dbType: String, folderName: String?) {
        val dbPath = backupsPath.resolve(dbType)
        if (!Files.isDirectory(dbPath)) {
            println("No backups found for the specified database type❗: $dbType")
            return
        }

        if (folderName == null) {
            listSubdirectories(dbPath)
        } else {
            listFilesInFolder(dbPath, folderName)
        }
    }

    @Throws(IOException::class)
    private fun listSubdirectories(dbPath: Path) {
        Files.list(dbPath)
            .filter { path: Path? -> Files.isDirectory(path) }
            .map<Path?> { obj: Path? -> obj!!.fileName }
            .forEach { x: Path? -> println(x) }
    }

    @Throws(IOException::class)
    private fun listFilesInFolder(dbPath: Path, folderName: String) {
        val folderPath = dbPath.resolve(folderName)
        if (!Files.isDirectory(folderPath)) {
            println("Folder not found❗: $folderName")
            return
        }

        Files.list(folderPath)
            .map<Path?> { obj: Path? -> obj!!.fileName }
            .forEach { x: Path? -> println(x) }
    }
}