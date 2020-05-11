package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Mono
import java.util.ArrayList

class FindUsersCommand : DiscordCommand() {

    override val name: String
        get() = "findusers"

    override val aliases: List<String>
        get() = listOf("fu")

    override val description: String
        get() = "Finds users with a given role"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, input: Role) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild
                .flatMapMany { it.members }
                .flatMap { Mono.zip(Mono.just(it), it.roles.collectList()) }
                .filter { x -> x.t2.contains(input) }
                .map { it.t1 }
                .collectList()
                .switchIfEmpty(Mono.just(ArrayList()))
                .flatMap { members ->
                    if (members.isEmpty()) {
                        commandManager.sendResponse(message.channel, "No users found with that role", "No users were found with the given role.")
                    } else {
                        val stringBuilder = StringBuilder()
                        members.forEach { x -> stringBuilder.append(x.mention).append(" | ").append(x.id.asString()).append('\n') }
                        commandManager.sendResponse(message.channel, members.size.toString() + " " + (if (members.size > 1) "Users" else "User") + " with the role \"" + input.name + "\"", stringBuilder.toString())
                    }
                }
                .subscribe()
    }

}
