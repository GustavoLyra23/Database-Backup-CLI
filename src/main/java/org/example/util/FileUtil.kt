package org.example.util;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

    private FileUtil() {
    }

    public static <T> T readDataJsonFile(String path, Class<T> clazz) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, clazz);
        }
    }
}
