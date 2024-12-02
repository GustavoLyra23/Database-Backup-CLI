package org.example;
import org.example.entities.DbConnectionEntity;
import org.example.factory.CommandFactory;
import org.example.service.Command;

import java.util.Scanner;

import static org.example.util.UI.showHelp;
import static org.example.util.UI.showInvalidCommand;

public class Main {
    private static final DbConnectionEntity dbConnectionEntity = new DbConnectionEntity();
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