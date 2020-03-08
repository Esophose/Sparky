package dev.esophose.discordbot.command.commands.owner;

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.math.max

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
                        println(resultMessages)
                        message.channel.subscribe { channel ->
                            for (resultMessage in resultMessages)
                                channel.createMessage("```$resultMessage```").subscribe()
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

        fun addToMessage(text: String) {
            val escapedText = text.replace("`", "\\`")
            if (message.length + escapedText.length > 2000 - 10) {
                messages.add(message.toString())
                message.clear()
            }
            message.append(escapedText)
        }

        // Create a list of row values
        // Add the column labels
        val rowValues = mutableListOf<MutableList<String>>()
        for (i in 0 until columnCount)
            rowValues.add(mutableListOf(rsmd.getColumnName(i + 1)))

        // Add data values
        while (result.next()) {
            for (i in 0 until columnCount) {
                var value = result.getString(i + 1).replace("\n", "")
                if (value.length >= 50)
                    value = value.substring(0..50) + "..."
                rowValues[i].add(value)
            }
            rowCount++
        }

        if (rowCount == 0)
            return mutableListOf("\nNo rows were found matching your query")

        // Create a list of max column length strings for padding
        val columnLengthPaddings = mutableListOf<String>()

        // Add max column lengths
        for (i in 0 until columnCount) {
            var length = 0
            for (value in rowValues[i])
                length = max(length, value.length)
            val sb = StringBuilder()
            for (k in 0 until length)
                sb.append(' ')
            columnLengthPaddings.add(sb.toString())
        }

        // Insert column header separators
        for (i in 0 until columnCount)
            rowValues[i].add(1, columnLengthPaddings[i].replace(' ', '-'))

        // TODO: First value should be centered

        // Build rows
        for (i in 0 until rowCount + 2) { // +2 for column headers
            val line = StringBuilder("| ")
            for (n in 0 until columnCount) {
                if (n > 0)
                    line.append(" | ")

                val rowValue = rowValues[n][i]
                line.append(rowValue).append(columnLengthPaddings[n].substring(rowValue.length))
            }
            line.append(" |\n");
            addToMessage(line.toString())
        }

        addToMessage("\nTotal rows: $rowCount")

        messages.add(message.toString())

        return messages
    }

}
