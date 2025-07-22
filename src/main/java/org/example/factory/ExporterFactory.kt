package org.example.factory;

import org.example.entities.ConnectionEntity;
import org.example.service.DatabaseExporter;
import org.example.service.impl.MongoDatabaseExporter;
import org.example.service.impl.SqlDatabaseExporter;

public class ExporterFactory {

    private ExporterFactory() {
    }

    public static DatabaseExporter createExporter(ConnectionEntity connectionEntity) {
        if ("SQL".equalsIgnoreCase(connectionEntity.getDbType())) {
            return new SqlDatabaseExporter(connectionEntity.getUrl(), connectionEntity.getUser(), connectionEntity.getPassword());
        }

        if ("MONGO".equalsIgnoreCase(connectionEntity.getDbType())) {
            return new MongoDatabaseExporter(connectionEntity.getUrl(), connectionEntity.getDbName());
        }
        throw new UnsupportedOperationException("Unsupported database❗...");
    }
}
