package dev.esophose.discordbot.command.commands.owner;

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.sql.ResultSet
import java.sql.SQLException

class DatabaseCommand : DiscordCommand(true) {

    override val name: String
        get() = "database"

    override val aliases: List<String>
        get() = listOf("db")

    override val description: String
        get() = "Sends a query to the database"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, query: String) {
        val commandManager = Sparky.getManager(CommandManager::class)

        Sparky.connector.connect { connection ->
            connection.createStatement().use { statement ->
                try {
                    if (query.startsWith("SELECT", true)) {
                        val resultMessages = this.getResultStrings(statement.executeQuery(query))
                        message.channel.subscribe { channel ->
                            resultMessages.forEach { channel.createMessage("```$it```").subscribe() }
                        }
                    } else {
                        val changedRows = statement.executeUpdate(query)
                        val changed = if (changedRows > 0) changedRows.toString() else "No"
                        commandManager.sendResponse(message.channel, "Success", "$changed rows were changed").subscribe()
                    }
                } catch (ex: SQLException) {
                    commandManager.sendResponse(message.channel, "Error", ex.message ?: "An unknown error occurred while executing the query").subscribe()
                }
            }
        }
    }

    private fun getResultStrings(result: ResultSet): List<String> {
        val rsmd = result.metaData
        val columnCount = rsmd.columnCount
        var rowCount = 0
        val messages = mutableListOf<String>()
        val message = StringBuilder()
        val s = StringBuilder()

        fun addToMessage(text: String) {
            val escapedText = text.replace("`", "\\`")
            if (message.length + escapedText.length > 2000 - 8) {
                messages.add(message.toString())
                message.clear()
            }
            message.append(escapedText)
        }

        while (result.next()) {
            s.clear()
            if (rowCount == 0) {
                s.append("| ")
                for (i in 1..columnCount) {
                    if (i > 1)
                        s.append(" | ")
                    val name = rsmd.getColumnName(i)
                    s.append(name)
                }
                s.append(" |\n")
            }
            rowCount++
            s.append("| ")
            for (i in 1..columnCount) {
                if (i > 1)
                    s.append(" | ")
                s.append(result.getString(i))
            }
            s.append(" |\n")

            addToMessage(s.toString())
            s.clear()
        }

        if (rowCount != 0) {
            addToMessage("\nTotal rows: $rowCount")
        } else {
            addToMessage("\nNo rows were found matching your query")
        }

        messages.add(message.toString())

        return messages
    }

}
