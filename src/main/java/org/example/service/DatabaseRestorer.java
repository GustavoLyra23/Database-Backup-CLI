package org.example.service;

import org.example.entities.DbConnectionEntity;

import java.util.List;

public interface DatabaseRestorer {

    void restoreDatabase(String key, List<String> saves, String fileDbType, String fileName, DbConnectionEntity dbConnectionEntity);
}
