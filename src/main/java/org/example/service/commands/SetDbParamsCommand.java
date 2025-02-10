package org.example.service.commands;

import org.example.entities.ConnectionEntity;
import org.example.service.Command;
import org.example.util.RegexUtil;

public class SetDbParamsCommand implements Command {
    private final ConnectionEntity connectionEntity;

    public SetDbParamsCommand(ConnectionEntity connectionEntity) {
        this.connectionEntity = connectionEntity;
    }

    @Override
    public void execute(String command) {
        var params = RegexUtil.getDbParams(command);
        if (params == null) {
            System.out.println("Invalid database parameters.");
            return;
        }

        connectionEntity.setDbType(params.get(0));
        connectionEntity.setUrl(params.get(1));
        connectionEntity.setPassword(params.size() > 2 && !params.get(2).isEmpty() ? params.get(2) : null);
        connectionEntity.setUser(params.size() > 3 && !params.get(3).isEmpty() ? params.get(3) : null);
        connectionEntity.setDbName(params.size() > 4 && params.get(4) != null ? params.get(4) : null);
        System.out.println("Database parameters set.");
    }
}

