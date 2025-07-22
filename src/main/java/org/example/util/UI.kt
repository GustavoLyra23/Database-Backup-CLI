package org.example.util

object UI {
    @JvmStatic
    fun showHelp() {
        println("Available Commands:")
        println("--generate key                      : Generates and displays an encryption key.")
        println("--db <dbType> --url <url>           : Sets the database parameters with optional parameters:")
        println("    [--password <password>] [--user <user>] [--dbName <database name>]")
        println("--do backup                         : Starts a backup process with optional parameters:")
        println("    [--entity <entity1, entity2>] [--key <encryption key>]")
        println("--restore                           : Restores a database backup with required parameters:")
        println("    --foldertypedb <mongo/sql> --folderName <folder name>")
        println("    [--saves <save1, save2>] [--key <encryption key>]")
        println("--list                              : Lists available backups with optional parameters:")
        println("    [mongo/sql]                     : List contents of the 'mongo' or 'sql' backup directory.")
        println("    [--folder <folder name>]        : Lists files within a specified subfolder inside 'mongo' or 'sql'.")
        println("--help                              : Displays this help message.")
    }

    @JvmStatic
    fun showInvalidCommand() {
        println("Invalid command❗... Use '--help' to see the list of available commands.")
    }

    @JvmStatic
    fun exportingDbMessage() {
        println("Exporting database \uD83D\uDCC2...")
    }
}
