package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet

class SetMessageCreateSecurityEnabledCommand : DiscordCommand() {

    override val name: String
        get() = "setmessagecreatesecurityenabled"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Sets whether or not message creation security is enabled"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, enabled: Boolean?) {
        val commandManager = Sparky.getManager(CommandManager::class)

        Sparky.getManager(GuildSettingsManager::class).updateMessageCreateSecurity(message.guildId, enabled!!)

        if (enabled) {
            commandManager.sendResponse(message.channel, "Enabled message creation security",
                    "Security for rapid message creation has been enabled. If too many message are sent in channel within a certain time frame, " + "the channel will be put into slowmode to prevent a raid and/or spam.").subscribe()
        } else {
            commandManager.sendResponse(message.channel, "Disabled message creation security",
                    "Security for rapid message creation has been disabled. Your server may be more vulnerable to raids and channel spam with this disabled.").subscribe()
        }
    }

}
