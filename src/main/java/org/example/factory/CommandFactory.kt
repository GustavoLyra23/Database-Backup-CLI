package org.example.factory;

import org.example.entities.ConnectionEntity;
import org.example.service.Command;
import org.example.service.commands.*;

public class CommandFactory {
    private final ConnectionEntity connectionEntity;

    public CommandFactory(ConnectionEntity connectionEntity) {
        this.connectionEntity = connectionEntity;
    }

    public Command getCommand(String command) {
        //love if else <3
        if (command.contains("--generate key")) {
            return new GenerateKeyCommand();
        } else if (command.contains("--db")) {
            return new SetDbParamsCommand(connectionEntity);
        } else if (command.contains("--do backup")) {
            return new DoBackupCommand(connectionEntity);
        } else if (command.contains("--restore")) {
            return new DoRestoreCommand(connectionEntity);
        } else if (command.contains("--list")) {
            return new ListAllCommand();
        } else {
            return null;
        }
    }
}