package org.example.factory

import org.example.entities.ConnectionEntity
import org.example.service.Command
import org.example.service.commands.*

class CommandFactory(private val connectionEntity: ConnectionEntity?) {
    fun getCommand(command: String): Command? {
        //love if else <3
        if (command.contains("--generate key")) {
            return GenerateKeyCommand()
        } else if (command.contains("--db")) {
            return SetDbParamsCommand(connectionEntity)
        } else if (command.contains("--do backup")) {
            return DoBackupCommand(connectionEntity)
        } else if (command.contains("--restore")) {
            return DoRestoreCommand(connectionEntity)
        } else if (command.contains("--list")) {
            return ListAllCommand()
        } else {
            return null
        }
    }
}