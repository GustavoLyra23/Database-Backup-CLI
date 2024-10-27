package org.example.util;

public class RegexUtil {

    private static final String generateKeyRegex = "--generate\\s+key";
    private static final String dbParamsRegex = "--db\\s+(\\S+)\\s+--url\\s+(\\S+)\\s+--password\\s+(\\S+)\\s+--user\\s+(\\S+)\\s+--host\\s+(\\S+)";
    private static final String doBackupRegex = "--do\\s+backup(\\s+--entity\\s+(\\S+))?(\\s+--key\\s+(\\S+))?";
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


}
