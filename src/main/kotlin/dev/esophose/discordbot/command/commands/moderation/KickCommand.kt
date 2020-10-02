package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import java.util.Optional

class KickCommand : DiscordCommand() {

    override val name: String
        get() = "kick"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Kicks a member"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.KICK_MEMBERS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.KICK_MEMBERS

    fun execute(message: DiscordCommandMessage, member: Member, reason: Optional<String>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        if (member.id == Sparky.self.id) {
            commandManager.sendResponse(message.channel, "Why would you do that?", "Why would you try to kick me? What have I ever done to you?").subscribe()
            return
        }

        message.guild
                .flatMap { x -> reason.map { s -> x.kick(member.id, s) }.orElseGet { x.kick(member.id) } }
                .doOnError { error -> commandManager.sendResponse(message.channel, "Failed to kick user", "An error occurred trying to kick that user: " + error.message).subscribe() }
                .doOnSuccess { commandManager.sendResponse(message.channel, "User kicked", member.mention + " has been kicked.").subscribe() }
                .subscribe()
    }

}
