package org.example.service.commands;

import org.example.entities.DbConnectionEntity;
import org.example.service.Command;
import org.example.util.RegexUtil;

public class SetDbParamsCommand implements Command {
    private final DbConnectionEntity dbConnectionEntity;

    public SetDbParamsCommand(DbConnectionEntity dbConnectionEntity) {
        this.dbConnectionEntity = dbConnectionEntity;
    }

    @Override
    public void execute(String command) {
        var params = RegexUtil.getDbParams(command);
        if (params == null) {
            System.out.println("Invalid database parameters.");
            return;
        }

        dbConnectionEntity.setDbType(params.get(0));
        dbConnectionEntity.setUrl(params.get(1));
        dbConnectionEntity.setPassword(params.size() > 2 && !params.get(2).isEmpty() ? params.get(2) : null);
        dbConnectionEntity.setUser(params.size() > 3 && !params.get(3).isEmpty() ? params.get(3) : null);
        dbConnectionEntity.setDbName(params.size() > 4 && params.get(4) != null ? params.get(4) : null);
        System.out.println("Database parameters set.");
    }
}

