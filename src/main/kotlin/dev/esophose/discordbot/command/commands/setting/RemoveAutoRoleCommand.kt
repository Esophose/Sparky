package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.misc.GuildSettings
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import java.util.Collections

class RemoveAutoRoleCommand : DiscordCommand() {

    override val name: String
        get() = "removeautorole"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Removes a role from being automatically given to new members on join"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, role: Role) {
        val commandManager = Sparky.getManager(CommandManager::class)
        val guildSettingsManager = Sparky.getManager(GuildSettingsManager::class)

        val guildSettings = guildSettingsManager.getGuildSettings(message.guildId)
        if (guildSettings.autoRoleIds.contains(role.id)) {
            commandManager.sendResponse(message.channel, "Auto role does not exist", "An auto role for " + role.mention + " does not exist.").subscribe()
            return
        }

        guildSettingsManager.addAutoRole(message.guildId, role.id)
        commandManager.sendResponse(message.channel, "Removed auto role", "The auto role for " + role.mention + " has been removed.").subscribe()
    }

}
