package org.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    private static final String generateKeyRegex = "--generate\\s+key";
    private static final String dbParamsRegex = "--db\\s+(\\S+)\\s+--url\\s+(\\S+)\\s+--password\\s+(\\S+)\\s+--user\\s+(\\S+)";
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

    public static String[] getDbParams(String input) {
        Matcher matcher = Pattern.compile(dbParamsRegex).matcher(input);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)};
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
            return matcher.group(1).replace("[", "").replace("]", "").split(",\\s*"); // Captura as entidades como uma lista separada por vírgulas
        }
        return null;
    }
}
