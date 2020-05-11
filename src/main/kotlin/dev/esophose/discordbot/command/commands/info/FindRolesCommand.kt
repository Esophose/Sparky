package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import org.apache.commons.lang3.StringUtils
import reactor.core.publisher.Mono
import java.util.*


class FindRolesCommand : DiscordCommand() {

    override val name: String
        get() = "findroles"

    override val aliases: List<String>
        get() = listOf("fr")

    override val description: String
        get() = "Finds roles matching the given input"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_ROLES

    fun execute(message: DiscordCommandMessage, input: String) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild
                .flatMapMany { it.roles }
                .filter { x -> input == "*" || StringUtils.containsIgnoreCase(x.name, input) }
                .sort(Comparator.comparingInt<Role> { it.rawPosition }.reversed())
                .collectList()
                .switchIfEmpty(Mono.just(ArrayList()))
                .flatMap { roles ->
                    if (roles.isEmpty()) {
                        commandManager.sendResponse(message.channel, "No roles found", "No roles were found matching your input.")
                    } else {
                        val stringBuilder = StringBuilder()
                        roles.forEach { x -> stringBuilder.append(x.mention).append(" | ").append(x.id.asString()).append('\n') }
                        commandManager.sendResponse(message.channel, roles.size.toString() + " " + (if (roles.size > 1) "Roles" else "Role") + " found matching \"" + input + "\"", stringBuilder.toString())
                    }
                }
                .subscribe()
    }

}
