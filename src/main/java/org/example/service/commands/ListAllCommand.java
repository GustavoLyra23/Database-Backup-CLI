package org.example.service.commands;

import org.example.service.Command;
import org.example.util.RegexUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ListAllCommand implements Command {
    @Override
    public void execute(String command) {
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
            System.out.println("Error while listing backups❗: " + e.getMessage());
        }
    }

    private void listRootDirectories(Path backupsPath) throws IOException {
        Files.list(backupsPath)
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .forEach(System.out::println);
    }

    private void listDatabaseTypeDirectories(Path backupsPath, String dbType, String folderName) throws IOException {
        Path dbPath = backupsPath.resolve(dbType);
        if (!Files.isDirectory(dbPath)) {
            System.out.println("No backups found for the specified database type❗: " + dbType);
            return;
        }

        if (folderName == null) {
            listSubdirectories(dbPath);
        } else {
            listFilesInFolder(dbPath, folderName);
        }
    }

    private void listSubdirectories(Path dbPath) throws IOException {
        Files.list(dbPath)
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .forEach(System.out::println);
    }

    private void listFilesInFolder(Path dbPath, String folderName) throws IOException {
        Path folderPath = dbPath.resolve(folderName);
        if (!Files.isDirectory(folderPath)) {
            System.out.println("Folder not found❗: " + folderName);
            return;
        }

        Files.list(folderPath)
                .map(Path::getFileName)
                .forEach(System.out::println);
    }
}