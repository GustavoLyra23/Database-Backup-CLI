package org.example.service;

import org.example.entities.ConnectionEntity;

import java.util.List;

public interface DatabaseRestorer {

    void restoreDatabase(String key, List<String> saves, String fileDbType, String fileName, ConnectionEntity connectionEntity);
}
