package org.example.util

import org.example.util.UI.showInvalidCommand
import java.util.List
import java.util.regex.Pattern

object RegexUtil {
    private const val generateKeyRegex = "--generate\\s+key"
    private val dbParamsRegex = "--db\\s+(\\S+)" +
            "\\s+--url\\s+(\\S+)" +
            "(?:\\s+--password\\s+(\\S+))?" +
            "(?:\\s+--user\\s+(\\S+))?" +
            "(?:\\s+--dbName\\s+(\\S+))?"
    private const val doBackupRegex = "--do\\s+backup(?:\\s+--entity\\s+(\\[?[\\w,\\s]+]?))?(?:\\s+--key\\s+(\\S+))?"
    private val restoreWithSavesAndKeyRegex = "--restore\\s+--foldertypedb\\s+(mongo|sql)" +
            "\\s+--folderName\\s+(\\S+)" +
            "(?:\\s+--saves\\s+\\[(\\s*\\w+(?:,\\s*\\w+)*\\s*)])?" +
            "(?:\\s+--key\\s+(\\S+))?"
    private const val listDbTypeRegex = "--list\\s+(mongo|sql)?(?:\\s+--folder\\s+(\\S+))?"


    fun isGenerateKey(input: String): Boolean {
        return input.matches(generateKeyRegex.toRegex())
    }

    fun isDbParams(input: String): Boolean {
        return input.matches(dbParamsRegex.toRegex())
    }

    fun isDoBackup(input: String): Boolean {
        return input.matches(doBackupRegex.toRegex())
    }

    fun isRestoreWithSavesAndKey(input: String): Boolean {
        return input.matches(restoreWithSavesAndKeyRegex.toRegex())
    }

    fun isListCommand(input: String): Boolean {
        return input.matches(listDbTypeRegex.toRegex())
    }


    fun getDbParams(input: String?): MutableList<String?>? {
        try {
            val matcher = Pattern.compile(dbParamsRegex).matcher(input)
            if (matcher.find()) {
                val db = matcher.group(1)
                val url = matcher.group(2)
                val password = matcher.group(3)
                val user = matcher.group(4)
                val dbName = matcher.group(5)
                return if (db.equals("SQL", ignoreCase = true)) List.of<String?>(
                    db,
                    url,
                    password,
                    user
                ) else List.of<String?>(
                    db,
                    url,
                    if (password != null) password else "",
                    if (user != null) user else "",
                    dbName
                )
            }
        } catch (e: Exception) {
            showInvalidCommand()
        }
        return null
    }

    fun getBackupKey(input: String?): String? {
        val matcher = Pattern.compile(doBackupRegex).matcher(input)
        if (matcher.find()) {
            return matcher.group(2)
        }
        return null
    }

    fun getBackupEntities(input: String?): Array<String?>? {
        val matcher = Pattern.compile(doBackupRegex).matcher(input)
        if (matcher.find() && matcher.group(1) != null) {
            return matcher.group(1).replace("[", "").replace("]", "").split(",\\s*".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        return null
    }

    fun getFileTypeDb(input: String?): String? {
        val matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    fun getFileName(input: String?): String? {
        val matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input)
        if (matcher.find()) {
            return matcher.group(2)
        }
        return null
    }

    fun getSaves(input: String?): Array<String?>? {
        val matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input)
        if (matcher.find() && matcher.group(3) != null) {
            return matcher.group(3).split(",\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        return null
    }

    fun getRestoreKey(input: String?): String? {
        val matcher = Pattern.compile(restoreWithSavesAndKeyRegex).matcher(input)
        if (matcher.find()) {
            return matcher.group(4)
        }
        return null
    }

    fun getDbType(input: String?): String? {
        val matcher = Pattern.compile(listDbTypeRegex).matcher(input)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    fun getFolderName(input: String?): String? {
        val matcher = Pattern.compile(listDbTypeRegex).matcher(input)
        if (matcher.find()) {
            return matcher.group(2)
        }
        return null
    }
}
