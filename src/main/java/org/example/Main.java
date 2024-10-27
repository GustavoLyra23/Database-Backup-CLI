package org.example;

import org.example.factory.ExporterFactory;
import org.example.service.DatabaseExporter;
import org.example.service.impl.EncryptionService;
import org.example.util.RegexUtil;

import java.util.Scanner;

public class Main {

    static String key;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        EncryptionService encryptionService = EncryptionService.getInstance();

        while (true) {
            var command = scanner.nextLine();
            checkCommand(command, encryptionService);
        }
    }

    private static void checkCommand(String command, EncryptionService encryptionService) throws Exception {
        if (RegexUtil.isGenerateKey(command)) {
            key = encryptionService.encodeKey(encryptionService.generateKey());
            System.out.println("Key: " + key);
        }
        if (RegexUtil.isDbParams(command) || RegexUtil.isDoBackup(command)) {
            DatabaseExporter exporter = ExporterFactory.createExporter("SQL", "jdbc:postgresql://localhost:5432/challenge-db", "postgres", "123");
            exporter.exportDatabase(key);
            System.out.println("DB params are correct");
        }
    }


}



