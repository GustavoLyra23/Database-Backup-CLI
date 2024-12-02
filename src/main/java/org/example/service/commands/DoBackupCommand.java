package org.example.service.commands;

import org.example.entities.DbConnectionEntity;
import org.example.factory.ExporterFactory;
import org.example.service.Command;
import org.example.service.DatabaseExporter;
import org.example.util.RegexUtil;

import java.util.List;

public class DoBackupCommand implements Command {
    private final DbConnectionEntity dbConnectionEntity;


    public DoBackupCommand(DbConnectionEntity dbConnectionEntity) {
        this.dbConnectionEntity = dbConnectionEntity;
    }

    @Override
    public void execute(String command) {
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
            System.out.println("Error while doing backup❗: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Unsupported database type❗.");
        }
    }
}
