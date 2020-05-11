package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import org.apache.commons.lang3.text.WordUtils
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.stream.Collectors

class UserInfoCommand : DiscordCommand() {

    override val name: String
        get() = "userinfo"

    override val aliases: List<String>
        get() = listOf("uinfo")

    override val description: String
        get() = "Displays info for a user"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage, member: Optional<Member>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        member.map { Mono.just(it) }
                .orElseGet { message.author }
                .subscribe { target ->
                    Mono.zip(target.highestRole.switchIfEmpty(message.guild.flatMap { it.everyoneRole }), target.color, target.presence, target.roles.collectList())
                            .subscribe { tuple ->
                                var roles = tuple.t4.stream().sorted(Comparator.comparingInt<Role> { it.rawPosition }.reversed()).map { it.mention }.collect(Collectors.joining(" "))
                                if (roles.isBlank())
                                    roles = "None"

                                val botTag = if (target.isBot) " [BOT]" else ""

                                val info = "**Snowflake:** " + target.id.asString() + '\n' +
                                        "**Tag:** " + target.mention + '\n' +
                                        "**Discord Join Time:** " + BotUtils.snowflakeAsDateTimeString(target.id) + '\n' +
                                        "**Guild Join Time:** " + BotUtils.formatDateTime(LocalDateTime.ofInstant(target.joinTime, ZoneOffset.UTC)) + '\n' +
                                        "**Main Role:** " + (if (tuple.t1.isEveryone) "@everyone" else tuple.t1.mention) + '\n' +
                                        "**Color:** " + BotUtils.toHexString(tuple.t2) + '\n' +
                                        "**Displayname:** " + target.displayName + '\n' +
                                        "**Status:** " + WordUtils.capitalize(tuple.t3.status.value) + '\n' +
                                        "**Presence:** " + BotUtils.presenceAsString(tuple.t3) + '\n' +
                                        "**Roles:** " + roles
                                commandManager.sendResponse(message.channel, "Info for " + target.username + '#' + target.discriminator + botTag, info, target.avatarUrl).subscribe()
                            }
                }
    }

}
