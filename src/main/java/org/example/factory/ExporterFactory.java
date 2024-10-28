package org.example.factory;

import org.example.entities.DbConnectionEntity;
import org.example.service.DatabaseExporter;
import org.example.service.impl.SqlDatabaseExporter;

public class ExporterFactory {

    private ExporterFactory() {
    }

    public static DatabaseExporter createExporter(DbConnectionEntity dbConnectionEntity) {
        if ("SQL".equalsIgnoreCase(dbConnectionEntity.getDbType())) {
            return new SqlDatabaseExporter(dbConnectionEntity.getUrl(), dbConnectionEntity.getUser(), dbConnectionEntity.getPassword());
        }
        throw new UnsupportedOperationException("Unsupported database...");
    }
}
