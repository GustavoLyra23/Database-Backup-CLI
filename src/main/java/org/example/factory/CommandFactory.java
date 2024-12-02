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
        if (command.startsWith("--generate key")) {
            return new GenerateKeyCommand();
        } else if (command.startsWith("--db")) {
            return new SetDbParamsCommand(dbConnectionEntity);
        } else if (command.startsWith("--do backup")) {
            return new DoBackupCommand(dbConnectionEntity);
        } else if (command.startsWith("--restore")) {
            return new DoRestoreCommand(dbConnectionEntity);
        } else if (command.startsWith("--list")) {
            return new ListAllCommand();
        }
        return null;
    }
}