package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet

class SetPermissionCommand : DiscordCommand() {

    override val name: String
        get() = "setpermission"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Sets the member permission required to use a command"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, command: DiscordCommand, permission: Permission) {
        Sparky.getManager(GuildSettingsManager::class).updateCommandPermission(message.guildId, command, permission)
        Sparky.getManager(CommandManager::class).sendResponse(message.channel, "Set command permission",
                "The permission for **." + command.name + "** has been changed to **" + permission.name + "**").subscribe()
    }

}
