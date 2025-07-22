package org.example.factory

import org.example.entities.ConnectionEntity
import org.example.service.DatabaseExporter
import org.example.service.impl.MongoDatabaseExporter
import org.example.service.impl.SqlDatabaseExporter

object ExporterFactory {
    @JvmStatic
    fun createExporter(connectionEntity: ConnectionEntity): DatabaseExporter {
        if ("SQL".equals(connectionEntity.dbType, ignoreCase = true)) {
            return SqlDatabaseExporter(connectionEntity.url, connectionEntity.user, connectionEntity.password)
        }

        if ("MONGO".equals(connectionEntity.dbType, ignoreCase = true)) {
            return MongoDatabaseExporter(connectionEntity.url, connectionEntity.dbName)
        }
        throw UnsupportedOperationException("Unsupported database❗...")
    }
}
