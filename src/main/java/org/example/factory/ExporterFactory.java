package org.example.factory;

import org.example.service.DatabaseExporter;
import org.example.service.impl.SqlDatabaseExporter;

public class ExporterFactory {
    public static DatabaseExporter createExporter(String dbType, String jdbcUrl, String user, String password) {
        if ("SQL".equalsIgnoreCase(dbType)) {
            return new SqlDatabaseExporter(jdbcUrl, user, password);
        }
        throw new UnsupportedOperationException("Unsupported database...");
    }
}
