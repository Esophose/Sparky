package dev.esophose.discordbot.command.commands.owner

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)

        Sparky.discord.guilds.collectList().flatMap { guilds ->
            if (guilds.isEmpty()) {
                commandManager.sendResponse(message.channel, "No guilds were found", "What the heck?")
            } else {
                Flux.fromIterable(guilds).flatMap { Mono.zip(Mono.just(it), it.requestMembers().count()) }.collectList().flatMap {
                    val stringBuilder = StringBuilder()
                    it.sortedByDescending { x -> x.t2 }.forEach { x -> stringBuilder.append(x.t1.name).append(" | ").append(x.t2).append((if (x.t2 == 1L) " Member" else " Members") + " | ").append(x.t1.id.asString()).append('\n') }
                    commandManager.sendResponse(message.channel, guilds.size.toString() + " " + (if (guilds.size > 1) "Guilds" else "Guild"), stringBuilder.toString())
                }
            }
        }.subscribe()
    }

}
