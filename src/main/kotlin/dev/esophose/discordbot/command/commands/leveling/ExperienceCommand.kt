package dev.esophose.discordbot.command.commands.leveling;

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import dev.esophose.discordbot.utils.ExperienceUtils
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.Optional

class ExperienceCommand : DiscordCommand() {

    override val name: String
        get() = "xp"

    override val aliases: List<String>
        get() = listOf("exp", "experience")

    override val description: String
        get() = "Gets a user's xp and level"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage, member: Optional<Member>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild.subscribe { guild ->
            member.map { Mono.just(it) }
                    .orElseGet { message.author }
                    .subscribe { target ->
                        Sparky.connector.connect { connection ->
                            connection.prepareStatement("SELECT datetime FROM message_audit WHERE type = 'create' AND author_id = ? AND guild_id = ? GROUP BY author_id, content ORDER BY datetime").use { statement ->
                                statement.setLong(1, target.id.asLong())
                                statement.setLong(2, guild.id.asLong())
                                val result = statement.executeQuery()

                                var totalMessages = 0
                                var validMessages = 0
                                var lastDate: Instant = Instant.EPOCH
                                while (result.next()) {
                                    val dateString = result.getString("datetime")
                                    val date = Instant.parse(dateString)
                                    totalMessages++
                                    if (date.toEpochMilli() - lastDate.toEpochMilli() >= THRESHOLD) {
                                        validMessages++
                                        lastDate = date
                                    }
                                }

                                val xp = ExperienceUtils.getXPForUser(target.id, validMessages)
                                val level = ExperienceUtils.getCurrentLevel(xp)
                                commandManager.sendResponse(message.channel,
                                        "Stats for ${target.username}#${target.discriminator}",
                                        "Level: $level\n" +
                                        "XP: $xp/${ExperienceUtils.getXPRequiredForLevel(level + 1)} (${ExperienceUtils.getXPToNextLevel(xp)} remaining)\n" +
                                        "Messages: $validMessages/$totalMessages (${BotUtils.round((validMessages / totalMessages.toDouble()) * 100)}%)\n",
                                        target.avatarUrl
                                ).subscribe()
                            }
                        }
                    }
        }
    }

    companion object {
        const val THRESHOLD = 1000 * 60 // One minute
    }

}
