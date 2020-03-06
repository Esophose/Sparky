package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import java.util.stream.Collectors

class CommandInfoCommand : DiscordCommand() {

    override val name: String
        get() = "cinfo"

    override val aliases: List<String>
        get() = listOf("commandinfo")

    override val description: String
        get() = "Displays info for a command"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage, command: DiscordCommand) {
        val commandManager = Sparky.getManager(CommandManager::class)
        val commandPrefix = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(message.guildId).commandPrefix

        val info = "**Command Name:** " + command.name + '\n'.toString() +
                "**Aliases:** " + "[" + command.aliases.joinToString(", ") + "]" + '\n'.toString() +
                "**Description:** " + command.description + '\n'.toString() +
                "**Parameters:** " + commandManager.getCommandUsage(command, false, commandPrefix) + '\n'.toString() +
                "**Required Member Permission:** " + command.getRequiredMemberPermission(message.guildId).name + '\n'.toString() +
                "**Required Bot Permissions:** " + "[" + command.requiredBotPermissions.stream().map { it.name }.collect(Collectors.joining(", ")) + "]"
        commandManager.sendResponse(message.channel, "Info for ." + command.name, info).subscribe()
    }

}
