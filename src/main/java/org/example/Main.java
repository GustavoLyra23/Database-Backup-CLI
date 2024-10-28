package org.example;

import org.example.entities.DbConnectionEntity;
import org.example.factory.ExporterFactory;
import org.example.service.DatabaseExporter;
import org.example.service.impl.DatabaseRestoreService;
import org.example.util.EncryptionUtil;
import org.example.util.RegexUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    static DbConnectionEntity dbConnectionEntity = new DbConnectionEntity();


    public static void main(String[] args) throws Exception {
        DatabaseRestoreService restoreService = DatabaseRestoreService.getInstance();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                var command = scanner.nextLine();
                checkCommand(command, restoreService);
            }
        }
    }


    public static void checkCommand(String command, DatabaseRestoreService restoreService) {
        Map<String, Runnable> commands = Map.of(
                "--generateKey", Main::generateKey,
                "--list", () -> listAll(restoreService),
                "--doBackup", Main::doBackup);

        if (RegexUtil.isDbParams(command)) {
            setDbParams(command);
        } else {
            commands.getOrDefault(command.toLowerCase(), Main::invalidCommand).run();
        }
    }

    private static void generateKey() {
        var key = EncryptionUtil.encodeKey(Objects.requireNonNull(EncryptionUtil.generateKey()));
        System.out.println("Key: " + key);
    }

    private static void setDbParams(String command) {
        var params = RegexUtil.getDbParams(command);
        if (params != null) {
            dbConnectionEntity = DbConnectionEntity.builder()
                    .dbType(params[0])
                    .url(params[1])
                    .password(params[2])
                    .user(params[3])
                    .build();
            System.out.println("Database parameters set.");
        } else {
            System.out.println("Invalid parameters.");
        }
    }

    private static void doBackup() {
        if (dbConnectionEntity == null || dbConnectionEntity.getDbType() == null) {
            System.out.println("Please set database parameters first.");
            return;
        }

        DatabaseExporter exporter = ExporterFactory.createExporter(dbConnectionEntity);
        exporter.exportDatabase(null, null);
        System.out.println("Backup done.");
    }

    private static void listAll(DatabaseRestoreService restoreService) {
        System.out.println(restoreService.listAll());
    }

    private static void invalidCommand() {
        System.out.println("Invalid command.");
    }


    }


}



