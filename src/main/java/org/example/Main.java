package org.example;

import org.example.entities.DbConnectionEntity;
import org.example.factory.ExporterFactory;
import org.example.service.DatabaseExporter;
import org.example.service.DatabaseRestorer;
import org.example.service.impl.MongoDatabaseRestorer;
import org.example.service.impl.SQLRestorer;
import org.example.util.EncryptionUtil;
import org.example.util.RegexUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    static DbConnectionEntity dbConnectionEntity = new DbConnectionEntity();

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                var command = scanner.nextLine();
                if (command.equalsIgnoreCase("--help")) {
                    showHelp();
                } else {
                    checkCommand(command);
                }
            }
        }
    }

    public static void checkCommand(String command) {
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
            doRestore(command);
            return;
        }

        if (RegexUtil.isListCommand(command)) {
            listAll(command);
            return;
        }

        invalidCommand();
    }

    private static void showHelp() {
        System.out.println("Available Commands:");
        System.out.println("--generate key                      : Generates and displays an encryption key.");
        System.out.println("--db <dbType> --url <url>           : Sets the database parameters with optional parameters:");
        System.out.println("    [--password <password>] [--user <user>] [--dbName <database name>]");
        System.out.println("--do backup                         : Starts a backup process with optional parameters:");
        System.out.println("    [--entity <entity1, entity2>] [--key <encryption key>]");
        System.out.println("--restore                           : Restores a database backup with required parameters:");
        System.out.println("    --foldertypedb <mongo/sql> --folderName <folder name>");
        System.out.println("    [--saves <save1, save2>] [--key <encryption key>]");
        System.out.println("--list                              : Lists available backups with optional parameters:");
        System.out.println("    [mongo/sql]                     : List contents of the 'mongo' or 'sql' backup directory.");
        System.out.println("    [--folder <folder name>]        : Lists files within a specified subfolder inside 'mongo' or 'sql'.");
        System.out.println("--help                              : Displays this help message.");
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

    private static void doRestore(String command) {
        DatabaseRestorer restoreService;
        String fileTypeDb = Objects.requireNonNull(RegexUtil.getFileTypeDb(command)).toLowerCase();
        if (fileTypeDb.equalsIgnoreCase("mongo")) {
            restoreService = MongoDatabaseRestorer.getInstance();
        } else {
            restoreService = SQLRestorer.getInstance();
        }

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

    private static void listAll(String command) {
        Path backupsPath = Path.of(System.getProperty("user.home") + "/backups");

        try {
            String dbType = RegexUtil.getDbType(command);
            String folderName = RegexUtil.getFolderName(command);

            if (dbType == null) {
                listRootDirectories(backupsPath);
            } else {
                listDatabaseTypeDirectories(backupsPath, dbType, folderName);
            }

        } catch (IOException e) {
            System.out.println("Error while listing backups: " + e.getMessage());
        }
    }

    private static void listRootDirectories(Path backupsPath) throws IOException {
        Files.list(backupsPath)
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .forEach(System.out::println);
    }

    private static void listDatabaseTypeDirectories(Path backupsPath, String dbType, String folderName) throws IOException {
        Path dbPath = backupsPath.resolve(dbType);
        if (!Files.isDirectory(dbPath)) {
            System.out.println("No backups found for the specified database type: " + dbType);
            return;
        }

        if (folderName == null) {
            listSubdirectories(dbPath);
        } else {
            listFilesInFolder(dbPath, folderName);
        }
    }

    private static void listSubdirectories(Path dbPath) throws IOException {
        Files.list(dbPath)
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .forEach(System.out::println);
    }

    private static void listFilesInFolder(Path dbPath, String folderName) throws IOException {
        Path folderPath = dbPath.resolve(folderName);
        if (!Files.isDirectory(folderPath)) {
            System.out.println("Folder not found: " + folderName);
            return;
        }

        Files.list(folderPath)
                .map(Path::getFileName)
                .forEach(System.out::println);
    }

    private static void invalidCommand() {
        System.out.println("Invalid command. Use '--help' to see the list of available commands.");
    }
}
