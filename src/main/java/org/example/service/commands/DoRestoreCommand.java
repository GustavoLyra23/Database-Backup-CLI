package org.example.service.commands;

import org.example.entities.DbConnectionEntity;
import org.example.service.Command;
import org.example.service.DatabaseRestorer;
import org.example.service.impl.MongoDatabaseRestorer;
import org.example.service.impl.SQLRestorer;
import org.example.util.RegexUtil;

import java.util.List;
import java.util.Objects;

public class DoRestoreCommand implements Command {
    private final DbConnectionEntity dbConnectionEntity;

    public DoRestoreCommand(DbConnectionEntity dbConnectionEntity) {
        this.dbConnectionEntity = dbConnectionEntity;
    }

    @Override
    public void execute(String command) {
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
            System.out.println("Error while restoring❗: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Unsupported fileTypeDb❗.");
        }
    }
}