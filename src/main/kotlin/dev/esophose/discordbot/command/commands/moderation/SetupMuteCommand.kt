package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.ExtendedPermissionOverwrite
import discord4j.core.`object`.PermissionOverwrite
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.channel.Category
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import discord4j.core.`object`.util.Snowflake
import java.util.Collections
import java.util.Optional
import org.apache.commons.lang3.StringUtils
import reactor.core.publisher.Mono

class SetupMuteCommand : DiscordCommand() {

    override val name: String
        get() = "setupmute"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Sets up the .mute command for all channels"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.MANAGE_CHANNELS, Permission.MANAGE_ROLES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MUTE_MEMBERS

    fun execute(message: DiscordCommandMessage) {
        Mono.zip(message.guild, message.guild.flatMapMany { it.roles }.collectList()).subscribe { tuple ->
            val guild = tuple.t1
            val roles = tuple.t2

            val optionalMutedRole = roles.stream().filter { x -> StringUtils.equalsIgnoreCase(x.name, "Muted") }.findFirst()
            if (optionalMutedRole.isPresent) {
                this.applyChannelRoleOverrides(message, guild, optionalMutedRole.get())
            } else {
                guild.createRole { spec ->
                    spec.setHoist(false)
                    spec.setMentionable(false)
                    spec.setName("Muted")
                    spec.setPermissions(PermissionSet.none())
                    spec.reason = "Setting up bot muted role"
                }.subscribe { role -> this.applyChannelRoleOverrides(message, guild, role) }
            }
        }
    }

    private fun applyChannelRoleOverrides(message: DiscordCommandMessage, guild: Guild, mutedRole: Role) {
        val commandManager = Sparky.getManager(CommandManager::class)
        val roleId = mutedRole.id
        guild.channels.flatMap { channel ->
            val optionalOverwrite = channel.getOverwriteForRole(roleId)
            if (optionalOverwrite.isPresent)
                return@flatMap Mono.empty<Void>()

            when (channel) {
                is TextChannel -> channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId, PermissionSet.none(), PermissionSet.of(Permission.SEND_MESSAGES)))
                is VoiceChannel -> channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId, PermissionSet.none(), PermissionSet.of(Permission.SPEAK)))
                is Category -> channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId, PermissionSet.none(), PermissionSet.of(Permission.SEND_MESSAGES, Permission.SPEAK)))
                else -> Mono.empty<Void>()
            }
        }.hasElements().flatMap { commandManager.sendResponse(message.channel, "Set up muted command", "The muted command has been set up.") }.subscribe()
    }

}
