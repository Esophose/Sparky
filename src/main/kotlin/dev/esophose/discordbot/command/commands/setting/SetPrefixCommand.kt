package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet

class SetPrefixCommand : DiscordCommand() {

    override val name: String
        get() = "setprefix"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Sets the bot prefix for this guild"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, prefix: String) {
        val commandManager = Sparky.getManager(CommandManager::class)

        if (prefix.isBlank()) {
            commandManager.sendResponse(message.channel, "Invalid prefix", "The prefix must contain at least one non-whitespace character").subscribe()
            return
        }

        Sparky.getManager(GuildSettingsManager::class).updateCommandPrefix(message.guildId, prefix.trim())

        commandManager.sendResponse(message.channel, "Set bot prefix", "The bot command prefix for this guild has been changed to `${prefix.trim()}`").subscribe()
    }

}
