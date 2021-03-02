package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Mono

class SudoCommand : DiscordCommand() {

    override val name: String
        get() = "sudo"

    override val aliases: List<String>
        get() = listOf("force")

    override val description: String
        get() = "Performs a command as another member"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_WEBHOOKS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, member: Member, command: String) {
        message.delete().subscribe()

        val commandManager = Sparky.getManager(CommandManager::class)
        val commandPrefix = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(message.guildId).commandPrefix
        member.avatar.flatMap { avatar ->
            message.channel.cast(TextChannel::class.java).flatMap { channel ->
                channel.createWebhook { spec ->
                    spec.setName(member.displayName)
                    spec.setAvatar(avatar)
                }.flatMap { webhook ->
                    webhook.execute { spec ->
                        spec.setContent(command)
                    }.then(webhook.delete())
                }
            }
        }.thenEmpty(Mono.fromRunnable {
            if (command.startsWith(commandPrefix))
                commandManager.executeCommand(message.guild, message.channel, message.channelId, message.messageId, member, command, commandPrefix)
        }).subscribe()
    }

}
