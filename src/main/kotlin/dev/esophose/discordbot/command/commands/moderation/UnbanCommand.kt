package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.common.util.Snowflake
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import java.util.Optional

class UnbanCommand : DiscordCommand() {

    override val name: String
        get() = "unban"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Lifts a member ban"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.BAN_MEMBERS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.BAN_MEMBERS

    fun execute(message: DiscordCommandMessage, memberId: Snowflake, reason: Optional<String>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild
                .flatMapMany { it.bans }
                .filter { ban -> ban.user.id == memberId }
                .hasElements()
                .subscribe { isBanned ->
                    if (!isBanned) {
                        commandManager.sendResponse(message.channel, "Failed to unban user", memberId.asString() + " is not banned.").subscribe()
                    } else {
                        message.guild
                                .flatMap { x -> reason.map { s -> x.unban(memberId, s) }.orElseGet { x.unban(memberId) } }
                                .doOnSuccess { commandManager.sendResponse(message.channel, "User unbanned", memberId.asString() + " has been unbanned.").subscribe() }
                                .subscribe()
                    }
                }
    }

}
