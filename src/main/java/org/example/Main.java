package org.example;

import org.example.entities.DbConnectionEntity;
import org.example.factory.ExporterFactory;
import org.example.service.DatabaseExporter;
import org.example.service.impl.DatabaseRestoreService;
import org.example.util.EncryptionUtil;
import org.example.util.RegexUtil;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    static DbConnectionEntity dbConnectionEntity = new DbConnectionEntity();

    public static void main(String[] args) {
        DatabaseRestoreService restoreService = DatabaseRestoreService.getInstance();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                var command = scanner.nextLine();
                checkCommand(command, restoreService);
            }
        }
    }

    public static void checkCommand(String command, DatabaseRestoreService restoreService) {
        if (RegexUtil.isGenerateKey(command)) {
            generateKey();
            return;
        }

        if (RegexUtil.isDbParams(command)) {
            setDbParams(command);
            return;
        }

        if (RegexUtil.isDoBackup(command)) {
            doBackup(command);
            return;
        }

        if (command.equalsIgnoreCase("--list")) {
            listAll(restoreService);
        } else {
            invalidCommand();
        }
    }

    private static void generateKey() {
        var key = EncryptionUtil.encodeKey(Objects.requireNonNull(EncryptionUtil.generateKey()));
        System.out.println("Key: " + key);
    }

    private static void setDbParams(String command) {
        var params = RegexUtil.getDbParams(command);
        if (params == null || params.size() < 2) {
            System.out.println("Invalid number of parameters.");
            return;
        }
        dbConnectionEntity = DbConnectionEntity.builder()
                .dbType(params.get(0))
                .url(params.get(1))
                .password(params.size() > 2 && !params.get(2).isEmpty() ? params.get(2) : null)
                .user(params.size() > 3 && !params.get(3).isEmpty() ? params.get(3) : null)
                .DbName(params.size() > 4 && !params.get(4).isEmpty() ? params.get(4) : null)
                .build();
        System.out.println("Database parameters set.");
    }

    private static void doBackup(String command) {
        if (dbConnectionEntity == null || dbConnectionEntity.getUrl() == null) {
            System.out.println("Please set database parameters first.");
            return;
        }

        String key = RegexUtil.getBackupKey(command);
        String[] entitiesArray = RegexUtil.getBackupEntities(command);
        List<String> entities = (entitiesArray != null) ? List.of(entitiesArray) : null;

        try {
            DatabaseExporter exporter = ExporterFactory.createExporter(dbConnectionEntity);
            exporter.exportDatabase(key, entities);
        } catch (IllegalArgumentException e) {
            System.out.println("Error while doing backup: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Unsupported database type.");
        }
    }

    private static void listAll(DatabaseRestoreService restoreService) {
        try {
            System.out.println(restoreService.listAll());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void invalidCommand() {
        System.out.println("Invalid command.");
    }
}
