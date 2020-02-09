package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import java.util.Collections
import java.util.Optional
import org.apache.commons.lang3.StringUtils

class UnmuteCommand : DiscordCommand() {

    override val name: String
        get() = "unmute"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Unmutes a member"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES, Permission.MUTE_MEMBERS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MUTE_MEMBERS

    fun execute(message: DiscordCommandMessage, member: Member) {
        val commandManager = Sparky.getManager(CommandManager::class)
        message.guild.flatMapMany { it.roles }.collectList().subscribe { roles ->
            val optionalMutedRole = roles.stream().filter { x -> StringUtils.equalsIgnoreCase(x.name, "Muted") }.findFirst()
            if (!optionalMutedRole.isPresent) {
                commandManager.sendResponse(message.channel, "Not set up", "The mute command is not yet set up. Please run `.setupmute` to set up the role and channel overrides.").subscribe()
            } else {
                member.roles
                        .filter { x -> x == optionalMutedRole.get() }
                        .hasElements()
                        .subscribe { hasRole ->
                            if (!hasRole) {
                                commandManager.sendResponse(message.channel, "Couldn't unmute member", member.nicknameMention + " is not muted.").subscribe()
                            } else {
                                member.removeRole(optionalMutedRole.get().id, "Member was unmuted").subscribe()
                                commandManager.sendResponse(message.channel, "Member unmuted", member.nicknameMention + " has been unmuted.").subscribe()
                            }
                        }
            }
        }
    }

}
