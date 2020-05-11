package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import java.util.regex.Matcher
import java.util.regex.Pattern

class EmbedCommand : DiscordCommand() {

    override val name: String
        get() = "embed"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Makes the embed a message"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, title: String, text: String) {
        message.delete().subscribe()

        val embedTitle = if (title == "null") null else title
        val embedText = text.replace(Pattern.quote("\\n").toRegex(), Matcher.quoteReplacement("\n"))
        Sparky.getManager(CommandManager::class).sendResponse(message.channel, embedTitle, embedText).subscribe()
    }

}
