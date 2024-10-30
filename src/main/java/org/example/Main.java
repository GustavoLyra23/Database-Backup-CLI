package org.example;

import org.example.entities.DbConnectionEntity;
import org.example.factory.ExporterFactory;
import org.example.service.DatabaseExporter;
import org.example.service.DatabaseRestorer;
import org.example.service.impl.SQLRestorer;
import org.example.util.EncryptionUtil;
import org.example.util.RegexUtil;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    static DbConnectionEntity dbConnectionEntity = new DbConnectionEntity();

    public static void main(String[] args) {
        DatabaseRestorer restoreService = SQLRestorer.getInstance();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                var command = scanner.nextLine();
                checkCommand(command, restoreService);
            }
        }
    }

    public static void checkCommand(String command, DatabaseRestorer restoreService) {
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

        if (RegexUtil.isRestoreWithSavesAndKey(command)) {
            doRestore(command, restoreService);
        } else {
            invalidCommand();
        }

//        if (command.equalsIgnoreCase("--list")) {
//            listAll(restoreService);
//        } else {
//            invalidCommand();
//        }

    }

    private static void doRestore(String command, DatabaseRestorer restoreService) {
        String fileTypeDb = Objects.requireNonNull(RegexUtil.getFileTypeDb(command)).toLowerCase();
        String fileName = RegexUtil.getFileName(command);
        String[] savesArray = RegexUtil.getSaves(command);
        List<String> saves = (savesArray != null) ? List.of(savesArray) : null;
        String key = RegexUtil.getRestoreKey(command);

        if (fileName == null || fileName.isEmpty()) {
            System.out.println("Invalid restore parameters. Please provide a valid fileTypeDb and fileName.");
            return;
        }
        try {
            restoreService.restoreDatabase(key, saves, fileTypeDb, fileName, dbConnectionEntity);
        } catch (IllegalArgumentException e) {
            System.out.println("Error while restoring: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Unsupported fileTypeDb.");

        }
    }



    private static void generateKey() {
        var key = EncryptionUtil.encodeKey(Objects.requireNonNull(EncryptionUtil.generateKey()));
        System.out.println("Key: " + key);
    }

    private static void setDbParams(String command) {
        var params = RegexUtil.getDbParams(command);
        if (params == null) {
            System.out.println("Invalid database parameters.");
            return;
        }
        dbConnectionEntity = DbConnectionEntity.builder()
                .dbType(params.get(0))
                .url(params.get(1))
                .password(params.size() > 2 && !params.get(2).isEmpty() ? params.get(2) : null)
                .user(params.size() > 3 && !params.get(3).isEmpty() ? params.get(3) : null)
                .DbName(params.size() > 4 && params.get(4) != null ? params.get(4) : null)
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

//    private static void listAll(DatabaseRestorer restoreService) {
//        try {
//            System.out.println(restoreService.listAll());
//        } catch (RuntimeException e) {
//            System.out.println(e.getMessage());
//        }
//    }

    private static void invalidCommand() {
        System.out.println("Invalid command.");
    }
}
