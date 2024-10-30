package org.example.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    private static final String generateKeyRegex = "--generate\\s+key";
    private static final String dbParamsRegex = "--db\\s+(\\S+)" +
            "\\s+--url\\s+(\\S+)" +
            "(?:\\s+--password\\s+(\\S+))?" +
            "(?:\\s+--user\\s+(\\S+))?" +
            "(?:\\s+--dbName\\s+(\\S+))?";
    private static final String doBackupRegex = "--do\\s+backup(?:\\s+--entity\\s+(\\[?[\\w,\\s]+]?))?(?:\\s+--key\\s+(\\S+))?";
    private static final String restoreWithSavesAndKeyRegex = "--restore\\s+--fileTypeDb\\s+(mongo|sql)" +
            "\\s+--fileName\\s+(\\S+)" +
            "(?:\\s+--saves\\s+\\[(\\s*\\w+(?:,\\s*\\w+)*\\s*)])?" +
            "(?:\\s+--key\\s+(\\S+))?";

    public static boolean isGenerateKey(String input) {
        return input.matches(generateKeyRegex);
    }

    public static boolean isDbParams(String input) {
        return input.matches(dbParamsRegex);
    }

    public static boolean isDoBackup(String input) {
        return input.matches(doBackupRegex);
    }

    public static boolean isRestoreWithSavesAndKey(String input) {
        return input.matches(restoreWithSavesAndKeyRegex);
    }

    public static List<String> getDbParams(String input) {
        try {
            Matcher matcher = Pattern.compile(dbParamsRegex).matcher(input);
            if (matcher.find()) {
                String db = matcher.group(1);
                String url = matcher.group(2);
                String password = matcher.group(3);
                String user = matcher.group(4);
                String dbName = matcher.group(5);
                return db.equalsIgnoreCase("SQL") ?
                        List.of(db, url, password, user) :
                        List.of(db, url, password != null ? password : "", user != null ? user : "", dbName);
            }
        } catch (Exception e) {
            System.out.println("Invalid db parameters. Please provide valid parameters.");
        }
        return null;
    }

    public static String getBackupKey(String input) {
        Matcher matcher = Pattern.compile(doBackupRegex).matcher(input);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String[] getBackupEntities(String input) {
        Matcher matcher = Pattern.compile(doBackupRegex).matcher(input);
        if (matcher.find() && matcher.group(1) != null) {
            return matcher.group(1).replace("[", "").replace("]", "").split(",\\s*");
        }
        return null;
    }

    public static String getFileTypeDb(String input) {
        Matcher matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getFileName(String input) {
        Matcher matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String[] getSaves(String input) {
        Matcher matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input);
        if (matcher.find() && matcher.group(3) != null) {
            return matcher.group(3).split(",\\s*");
        }
        return null;
    }

    public static String getRestoreKey(String input) {
        Matcher matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input);
        if (matcher.find()) {
            return matcher.group(4);
        }
        return null;
    }
}
