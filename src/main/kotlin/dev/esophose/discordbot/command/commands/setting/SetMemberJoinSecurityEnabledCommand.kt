package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet

class SetMemberJoinSecurityEnabledCommand : DiscordCommand() {

    override val name: String
        get() = "setmemberjoinsecurityenabled"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Sets whether or not rapid member join security is enabled"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, enabled: Boolean?) {
        val commandManager = Sparky.getManager(CommandManager::class)

        Sparky.getManager(GuildSettingsManager::class).updateMemberJoinSecurity(message.guildId, enabled!!)

        if (enabled) {
            commandManager.sendResponse(message.channel, "Enabled member join security",
                    "Security for rapid member joining has been enabled. If too many members join within a certain time frame, " + "joining will be temporarily disabled to prevent a raid.").subscribe()
        } else {
            commandManager.sendResponse(message.channel, "Disabled member join security",
                    "Security for rapid member joining has been disabled. Your server may be more vulnerable to raids with this disabled.").subscribe()
        }
    }

}
