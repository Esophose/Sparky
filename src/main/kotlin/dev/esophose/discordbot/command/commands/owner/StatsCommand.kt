package dev.esophose.discordbot.command.commands.owner

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet

class StatsCommand : DiscordCommand(true) {

    override val name: String
        get() = "stats"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Views bot stats"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)

        val usedMemory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()
        val totalMemory = Runtime.getRuntime().maxMemory()

        val stats = """
            Processors: ${Runtime.getRuntime().availableProcessors()}
            Memory: $usedMemory/$totalMemory
        """.trimIndent()

        commandManager.sendResponse(message.channel, "Bot Stats", stats).subscribe()
    }

}
