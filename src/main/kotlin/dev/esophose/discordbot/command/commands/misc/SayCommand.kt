package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet

class SayCommand : DiscordCommand() {

    override val name: String
        get() = "say"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Makes the bot say a message"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, text: String) {
        message.delete().subscribe()
        message.channel
                .cast(GuildMessageChannel::class.java)
                .flatMap { it.createMessage(text) }
                .subscribe()
    }

}
