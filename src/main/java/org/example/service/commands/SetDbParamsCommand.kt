package org.example.service.commands

import org.example.entities.ConnectionEntity
import org.example.service.Command
import org.example.util.RegexUtil

class SetDbParamsCommand(private val connectionEntity: ConnectionEntity?) : Command {
    override fun execute(command: String?) {
        val params = RegexUtil.getDbParams(command)
        if (params == null) {
            println("Invalid database parameters.")
            return
        }

        connectionEntity?.dbType = params.get(0)
        connectionEntity?.url = params.get(1)
        connectionEntity?.password = if (params.size > 2 && !params[2]!!.isEmpty()) params[2] else null
        connectionEntity?.user = if (params.size > 3 && !params[3]!!.isEmpty()) params[3] else null
        connectionEntity?.dbName = if (params.size > 4 && params[4] != null) params[4] else null
        println("Database parameters set.")
    }
}

