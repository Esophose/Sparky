package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import java.util.Collections

class PurgeCommand : DiscordCommand() {

    override val name: String
        get() = "purge"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Purges a section of chat"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.MANAGE_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_MESSAGES

    fun execute(message: DiscordCommandMessage, amount: Int?) {
        message.delete().subscribe()
        message.channel
                .cast(GuildMessageChannel::class.java)
                .subscribe { channel ->
                    channel.bulkDelete(
                            channel.getMessagesBefore(channel.lastMessageId.orElse(message.messageId))
                                    .take(amount!!.toLong())
                                    .map { it.id })
                                    .subscribe()
                }
    }

}
