package org.example.service;

import java.util.List;

public interface DatabaseExporter {
    void exportDatabase(String key, List<String> entities);
}

