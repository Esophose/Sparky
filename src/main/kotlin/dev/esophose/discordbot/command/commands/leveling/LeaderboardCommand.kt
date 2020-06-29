package dev.esophose.discordbot.command.commands.leveling;

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.ExperienceUtils
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import discord4j.rest.util.Snowflake
import reactor.core.publisher.Flux
import java.time.Instant

class LeaderboardCommand : DiscordCommand() {

    override val name: String
        get() = "top"

    override val aliases: List<String>
        get() = listOf("leaderboard", "levels")

    override val description: String
        get() = "Displays the leveling leaderboard"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild.subscribe { guild ->
            Sparky.connector.connect { connection ->
                connection.prepareStatement("SELECT author_id, datetime FROM message_audit WHERE type = 'create' AND guild_id = ? GROUP BY content ORDER BY author_id, datetime").use { statement ->
                    statement.setLong(1, guild.id.asLong())
                    val result = statement.executeQuery()

                    val xpAmounts = mutableMapOf<Snowflake, Int>()
                    var lastAuthor: Snowflake? = null
                    var lastDate: Instant = Instant.EPOCH
                    var validMessages = 0
                    while (result.next()) {
                        val authorId = Snowflake.of(result.getLong("author_id"))
                        if (authorId != lastAuthor) {
                            if (lastAuthor != null) {
                                xpAmounts[lastAuthor] = ExperienceUtils.getXPForUser(authorId, validMessages)
                                lastDate = Instant.EPOCH
                                validMessages = 0
                            }
                            lastAuthor = authorId
                        }

                        val dateString = result.getString("datetime")
                        val date = Instant.parse(dateString)
                        if (date.toEpochMilli() - lastDate.toEpochMilli() >= THRESHOLD) {
                            validMessages++
                            lastDate = date
                        }
                    }

                    // TODO: This sorting algorithm is terrible
                    val leaderboard = mutableListOf<Snowflake>()
                    while (leaderboard.size < 10 && leaderboard.size < xpAmounts.size) {
                        var highest = 0
                        var highestAuthor: Snowflake? = null
                        for (entry in xpAmounts) {
                            if (leaderboard.contains(entry.key))
                                continue
                            if (entry.value > highest) {
                                highest = entry.value
                                highestAuthor = entry.key
                            }
                        }
                        if (highestAuthor != null)
                            leaderboard.add(highestAuthor)
                    }

                    Flux.fromIterable(leaderboard).flatMapSequential(Sparky.discord::getUserById).collectList().subscribe {
                        val leaderboardString = StringBuilder()
                        var placement = 1
                        for (leader in it) {
                            if (leaderboardString.isNotEmpty())
                                leaderboardString.append('\n')
                            val xp = xpAmounts[leader.id]!!
                            val level = ExperienceUtils.getCurrentLevel(xp)
                            leaderboardString.append("**$placement).** ${leader.username}#${leader.discriminator} (${leader.mention}) | Level $level (${xp}xp)")
                            placement++
                        }
                        commandManager.sendResponse(message.channel, "Leveling Leaderboard for ${guild.name}", leaderboardString.toString(), it[0].avatarUrl).subscribe()
                    }
                }
            }
        }
    }

    companion object {
        const val THRESHOLD = 1000 * 60 // One minute
    }

}
