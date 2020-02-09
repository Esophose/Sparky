package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.misc.GuildSettings
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import java.util.Collections

class AddAutoRoleCommand : DiscordCommand() {

    override val name: String
        get() = "addautorole"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Adds a role to be automatically given to new members on join"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, role: Role) {
        val commandManager = Sparky.getManager(CommandManager::class)
        val guildSettingsManager = Sparky.getManager(GuildSettingsManager::class)

        if (role.isEveryone) {
            commandManager.sendResponse(message.channel, "Don't be silly", "You can't add an auto role for `@everyone`... They already have that by default.").subscribe()
            return
        }

        val guildSettings = guildSettingsManager!!.getGuildSettings(message.guildId)
        if (guildSettings.autoRoleIds.contains(role.id)) {
            commandManager.sendResponse(message.channel, "Auto role already exists", "An auto role for ${role.mention} already exists.").subscribe()
            return
        }

        message.guild.flatMap { guild -> guild.getMemberById(Sparky.self!!.id) }
                .flatMap { it.highestRole }
                .subscribe { highestRole ->
                    highestRole.position
                            .zipWith(role.position)
                            .subscribe { tuple ->
                                if (tuple.t1 <= tuple.t2) {
                                    commandManager.sendResponse(message.channel, "Auto role hierarchy issue", "My highest role is ${highestRole.mention} but ${role.mention} has a lower role position. Move the desired auto role below my highest role then try this command again.").subscribe()
                                } else {
                                    guildSettingsManager.addAutoRole(message.guildId, role.id)
                                    commandManager.sendResponse(message.channel, "Added auto role", "An auto role for " + role.mention + " has been added.").subscribe()
                                }
                            }
                }
    }

}
