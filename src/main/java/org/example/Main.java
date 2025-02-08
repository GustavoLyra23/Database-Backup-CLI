package org.example;

import org.example.entities.DbConnectionEntity;
import org.example.factory.CommandFactory;
import org.example.service.Command;

import java.io.IOException;
import java.util.Scanner;

import static org.example.util.JsonFileUtil.readDataJsonFile;
import static org.example.util.UI.showHelp;
import static org.example.util.UI.showInvalidCommand;

public class Main {

    private static DbConnectionEntity dbConnectionEntity;

    // This block of code is responsible for loading the data from the data.json file and
    // storing it in the dbConnectionEntity variable.
    static {
        try {
            dbConnectionEntity = readDataJsonFile("C:\\Users\\gustavoml\\Downloads\\Database-Backup-CLI\\src\\main\\java\\resources\\data.json", DbConnectionEntity.class);
            System.out.println("Loaded data from data.json");
        } catch (IOException e) {
            System.out.println("Could not load database data from data.json... please enter the data through the console");
            dbConnectionEntity = new DbConnectionEntity();
        }
    }

    public static void main(String[] args) {
        CommandFactory commandFactory = new CommandFactory(dbConnectionEntity);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                var command = scanner.nextLine();
                if (command.equalsIgnoreCase("--help")) {
                    showHelp();
                } else {
                    Command cmd = commandFactory.getCommand(command);
                    if (cmd != null) {
                        cmd.execute(command);
                    } else {
                        showInvalidCommand();
                    }
                    }
                }
            }
        }
    }