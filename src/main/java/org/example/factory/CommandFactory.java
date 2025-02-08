package org.example.factory;

import org.example.entities.DbConnectionEntity;
import org.example.service.Command;
import org.example.service.commands.*;

public class CommandFactory {
    private final DbConnectionEntity dbConnectionEntity;

    public CommandFactory(DbConnectionEntity dbConnectionEntity) {
        this.dbConnectionEntity = dbConnectionEntity;
    }

    public Command getCommand(String command) {
        //love if else <3
        if (command.contains("--generate key")) {
            return new GenerateKeyCommand();
        } else if (command.contains("--db")) {
            return new SetDbParamsCommand(dbConnectionEntity);
        } else if (command.contains("--do backup")) {
            return new DoBackupCommand(dbConnectionEntity);
        } else if (command.contains("--restore")) {
            return new DoRestoreCommand(dbConnectionEntity);
        } else if (command.contains("--list")) {
            return new ListAllCommand();
        } else {
            return null;
        }
    }
}