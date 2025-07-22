package org.example.service.commands

import org.example.entities.ConnectionEntity
import org.example.service.Command
import org.example.service.DatabaseRestorer
import org.example.service.impl.MongoDatabaseRestorer
import org.example.service.impl.SQLRestorer
import org.example.util.RegexUtil
import java.util.*
import java.util.List

class DoRestoreCommand(private val connectionEntity: ConnectionEntity?) : Command {
    @Synchronized
    override fun execute(command: String?) {
        val restoreService: DatabaseRestorer?
        val fileTypeDb =
            Objects.requireNonNull<String?>(RegexUtil.getFileTypeDb(command)).lowercase(Locale.getDefault())
        if (fileTypeDb.equals("mongo", ignoreCase = true)) {
            restoreService = MongoDatabaseRestorer.instance
        } else {
            restoreService = SQLRestorer.instance
        }

        val fileName = RegexUtil.getFileName(command)
        val savesArray = RegexUtil.getSaves(command)
        val saves = if (savesArray != null) List.of<String?>(*savesArray) else null
        val key = RegexUtil.getRestoreKey(command)

        if (fileName == null || fileName.isEmpty()) {
            println("Invalid restore parameters. Please provide a valid fileTypeDb and fileName.")
            return
        }
        try {
            restoreService.restoreDatabase(key, saves, fileTypeDb, fileName, connectionEntity)
        } catch (e: IllegalArgumentException) {
            println("Error while restoring❗: " + e.message)
        } catch (e: UnsupportedOperationException) {
            println("Unsupported fileTypeDb❗.")
        }
    }
}