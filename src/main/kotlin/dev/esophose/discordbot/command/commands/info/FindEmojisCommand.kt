package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import org.apache.commons.lang3.StringUtils
import reactor.core.publisher.Mono
import java.util.*


class FindEmojisCommand : DiscordCommand() {

    override val name: String
        get() = "findemojis"

    override val aliases: List<String>
        get() = listOf("fe")

    override val description: String
        get() = "Finds emojis matching the given input"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_EMOJIS

    fun execute(message: DiscordCommandMessage, input: String) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild
                .flatMapMany { it.emojis }
                .filter { x -> input == "*" || StringUtils.containsIgnoreCase(x.name, input) }
                .collectList()
                .switchIfEmpty(Mono.just(ArrayList()))
                .flatMap { roles ->
                    if (roles.isEmpty()) {
                        commandManager.sendResponse(message.channel, "No emojis found", "No emojis were found matching your input.")
                    } else {
                        val stringBuilder = StringBuilder()
                        roles.forEach { x -> stringBuilder.append(x.asFormat()).append(" | ").append(x.name).append(" | ").append(x.id.asString()).append('\n') }
                        commandManager.sendResponse(message.channel, roles.size.toString() + " " + (if (roles.size > 1) "Emojis" else "Emoji") + " found matching \"" + input + "\"", stringBuilder.toString())
                    }
                }
                .subscribe()
    }

}
