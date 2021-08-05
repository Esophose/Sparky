package dev.esophose.discordbot.command.commands.owner

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet

class GuildsCommand : DiscordCommand(true) {

    override val name: String
        get() = "guilds"

    override val aliases: List<String>
        get() = listOf()

    override val description: String
        get() = "Lists all guilds the bot is in"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)

        Sparky.discord.guilds.collectList().flatMap { guilds ->
            if (guilds.isEmpty()) {
                commandManager.sendResponse(message.channel, "No guilds were found", "What the heck?")
            } else {
                val stringBuilder = StringBuilder()
                guilds.sortedByDescending { it.memberCount }.forEach { stringBuilder.append(it.name).append(" | ").append(it.memberCount).append((if (it.memberCount == 1) " Member" else " Members") + " | ").append(it.id.asString()).append('\n') }
                commandManager.sendResponse(message.channel, guilds.size.toString() + " " + (if (guilds.size > 1) "Guilds" else "Guild"), stringBuilder.toString())
            }
        }.subscribe()
    }

}
