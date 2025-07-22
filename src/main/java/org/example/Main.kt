package org.example;

import org.example.entities.ConnectionEntity;
import org.example.factory.CommandFactory;
import org.example.service.Command;
import org.example.util.Batch;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.util.FileUtil.readDataJsonFile;
import static org.example.util.UI.showHelp;
import static org.example.util.UI.showInvalidCommand;

public class Main {

    private static final String JSON_FILE_PATH = "C:\\Users\\gustavoml\\Downloads\\Database-Backup-CLI\\src\\main\\java\\resources\\data.json";
    private static ConnectionEntity connectionEntity;
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    /* This block of code is responsible for loading the data from the data.json file and
     storing it in the connectionEntity variable...  if it does not exist, it will create a new instance of ConnectionEntity
     and store it in the connectionEntity variable
    */
    static {
        try {
            connectionEntity = readDataJsonFile(JSON_FILE_PATH, ConnectionEntity.class);
            System.out.println("Loaded data from data.json");
        } catch (IOException e) {
            System.out.println("Could not load database data from data.json... please enter the data through the console");
            connectionEntity = new ConnectionEntity();
        }
    }

    public static void main(String[] args) {
        final CommandFactory commandFactory = new CommandFactory(connectionEntity);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                var command = scanner.nextLine();
                if (command.equalsIgnoreCase("--help")) {
                    showHelp();
                } else if (command.equalsIgnoreCase("--batch") && atomicInteger.get() == 0) {
                    //start batch process...
                    Batch.process(2, () -> commandFactory.getCommand("--do backup").execute(command));
                    atomicInteger.set(1);
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