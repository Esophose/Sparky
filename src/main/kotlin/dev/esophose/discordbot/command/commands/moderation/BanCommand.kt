package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import java.util.*

class BanCommand : DiscordCommand() {

    override val name: String
        get() = "ban"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Bans a member"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.BAN_MEMBERS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.BAN_MEMBERS

    fun execute(message: DiscordCommandMessage, member: Member, reason: Optional<String>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        if (member.id == Sparky.self.id) {
            commandManager.sendResponse(message.channel, "Why would you do that?", "Why would you try to ban me? What have I ever done to you?").subscribe()
            return
        }

        message.guild
                .flatMap { x -> x.ban(member.id) { spec -> reason.ifPresent { spec.reason = it } } }
                .doOnError { error -> commandManager.sendResponse(message.channel, "Failed to ban user", "An error occurred trying to ban that user: ${error.message}").subscribe() }
                .doOnSuccess { commandManager.sendResponse(message.channel, "User banned", "${member.mention} has been banned.").subscribe() }
                .subscribe()
    }

}
