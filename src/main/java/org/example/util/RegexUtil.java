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
    private static final String restoreRegex = "--restore\\s+(\\S+)(\\s+--key\\s+(\\S+))?";

    public static boolean isGenerateKey(String input) {
        return input.matches(generateKeyRegex);
    }

    public static boolean isDbParams(String input) {
        return input.matches(dbParamsRegex);
    }

    public static boolean isDoBackup(String input) {
        return input.matches(doBackupRegex);
    }

    public static boolean isRestore(String input) {
        return input.matches(restoreRegex);
    }

    public static List<String> getDbParams(String input) {
        Matcher matcher = Pattern.compile(dbParamsRegex).matcher(input);
        if (matcher.find()) {
            String db = matcher.group(1);
            String url = matcher.group(2);
            String password = matcher.group(3);
            String user = matcher.group(4);
            String dbName = matcher.group(5);
            return List.of(db, url, password != null ? password : "", user != null ? user : "", dbName);
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
}
