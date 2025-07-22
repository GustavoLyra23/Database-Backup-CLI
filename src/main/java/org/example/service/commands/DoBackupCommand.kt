package org.example.service.commands

import org.example.entities.ConnectionEntity
import org.example.factory.ExporterFactory.createExporter
import org.example.service.Command
import org.example.util.RegexUtil
import java.util.List

class DoBackupCommand(private val connectionEntity: ConnectionEntity?) : Command {
    @Synchronized
    override fun execute(command: String?) {
        if (connectionEntity == null || connectionEntity.url == null) {
            println("Please set database parameters first.")
            return
        }
        val key = RegexUtil.getBackupKey(command)
        val entitiesArray = RegexUtil.getBackupEntities(command)
        val entities = if (entitiesArray != null) listOf(*entitiesArray) else null
        try {
            val exporter = createExporter(connectionEntity)
            exporter.exportDatabase(key, entities)
        } catch (e: IllegalArgumentException) {
            println("Error while doing backup❗: " + e.message)
        } catch (e: UnsupportedOperationException) {
            println("Unsupported database type❗.")
        }
    }
}
