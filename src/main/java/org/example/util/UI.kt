package org.example.util;

public class UI {

    public static void showHelp() {
        System.out.println("Available Commands:");
        System.out.println("--generate key                      : Generates and displays an encryption key.");
        System.out.println("--db <dbType> --url <url>           : Sets the database parameters with optional parameters:");
        System.out.println("    [--password <password>] [--user <user>] [--dbName <database name>]");
        System.out.println("--do backup                         : Starts a backup process with optional parameters:");
        System.out.println("    [--entity <entity1, entity2>] [--key <encryption key>]");
        System.out.println("--restore                           : Restores a database backup with required parameters:");
        System.out.println("    --foldertypedb <mongo/sql> --folderName <folder name>");
        System.out.println("    [--saves <save1, save2>] [--key <encryption key>]");
        System.out.println("--list                              : Lists available backups with optional parameters:");
        System.out.println("    [mongo/sql]                     : List contents of the 'mongo' or 'sql' backup directory.");
        System.out.println("    [--folder <folder name>]        : Lists files within a specified subfolder inside 'mongo' or 'sql'.");
        System.out.println("--help                              : Displays this help message.");
    }

    public static void showInvalidCommand() {
        System.out.println("Invalid command❗... Use '--help' to see the list of available commands.");
    }

    public static void exportingDbMessage() {
        System.out.println("Exporting database \uD83D\uDCC2...");
    }


}
