package org.example

import org.example.entities.ConnectionEntity
import org.example.factory.CommandFactory
import org.example.util.Batch.Companion.process
import org.example.util.FileUtil.readDataJsonFile
import org.example.util.UI.showHelp
import org.example.util.UI.showInvalidCommand
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object Main {
    private const val JSON_FILE_PATH =
        "C:\\Users\\gustavoml\\Downloads\\Database-Backup-CLI\\src\\main\\java\\resources\\data.json"
    private var connectionEntity: ConnectionEntity? = null
    private val atomicInteger = AtomicInteger(0)

    /* This block of code is responsible for loading the data from the data.json file and
     storing it in the connectionEntity variable...  if it does not exist, it will create a new instance of ConnectionEntity
     and store it in the connectionEntity variable
    */
    init {
        try {
            connectionEntity = readDataJsonFile(
                JSON_FILE_PATH,
                ConnectionEntity::class.java as Class<ConnectionEntity?>
            )
            println("Loaded data from data.json")
        } catch (e: IOException) {
            println("Could not load database data from data.json... please enter the data through the console")
            connectionEntity = ConnectionEntity()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val commandFactory = CommandFactory(connectionEntity)
        Scanner(System.`in`).use { scanner ->
            while (true) {
                print(">")
                val command = scanner.nextLine()
                if (command.equals("--help", ignoreCase = true)) {
                    showHelp()
                } else if (command.equals("--batch", ignoreCase = true) && atomicInteger.get() == 0) {
                    //start a batch process...
                    process(2, Runnable { commandFactory.getCommand("--do backup")!!.execute(command) })
                    atomicInteger.set(1)
                } else {
                    val cmd = commandFactory.getCommand(command)
                    if (cmd != null) {
                        cmd.execute(command)
                    } else {
                        showInvalidCommand()
                    }
                }
            }
        }
    }
}