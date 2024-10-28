package org.example.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseRestoreService {

    private static final DatabaseRestoreService instance = new DatabaseRestoreService();

    private DatabaseRestoreService() {
    }

    public static DatabaseRestoreService getInstance() {
        return instance;
    }

    public void restoreDatabase(String key) {
        // Restore database
    }

    public String listAll() {
        var path = Paths.get(System.getProperty("user.home"), "backups");
        if (Files.isDirectory(path)) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("Folders: \n");
                Files.list(path).forEach(p ->
                        sb.append(p.getFileName())
                                .append("\n"));
                return sb.toString();
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while listing the directory contents.", e);
            }
        } else {
            throw new IllegalArgumentException("The path is not a directory: " + path);
        }
    }
}


