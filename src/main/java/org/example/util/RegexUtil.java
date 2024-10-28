package org.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    private static final String generateKeyRegex = "--generate\\s+key";
    private static final String dbParamsRegex = "--db\\s+(\\S+)\\s+--url\\s+(\\S+)\\s+--password\\s+(\\S+)\\s+--user\\s+(\\S+)";
    private static final String doBackupRegex = "--do\\s+backup(\\s+--entity\\s+(\\[?\\s*[\\w,\\s]+]?))?(\\s+--key\\s+(\\S+))?";
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
        Pattern pattern = Pattern.compile(dbParamsRegex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String db = matcher.group(1);
            String url = matcher.group(2);
            String password = matcher.group(3);
            String user = matcher.group(4);
            return new String[]{db, url, password, user};
        }
        return null;
    }




}
