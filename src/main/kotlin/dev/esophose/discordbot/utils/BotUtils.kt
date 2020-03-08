package dev.esophose.discordbot.utils

import dev.esophose.discordbot.Sparky
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono
import java.awt.Color
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

object BotUtils {

    private val FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")

    val watchingUserCount: Mono<Long>
        get() = Sparky.discord.users.filter { x -> !x.isBot }.distinct().count()

    fun presenceAsString(presence: Presence): String {
        val optionalActivity = presence.activity
        if (optionalActivity.isEmpty)
            return "Doing nothing"

        val activity = optionalActivity.get()
        return when (activity.type) {
            Activity.Type.PLAYING -> "Playing ${activity.name}"
            Activity.Type.STREAMING -> "Streaming ${activity.name}"
            Activity.Type.LISTENING -> "Listening to ${activity.name}"
            Activity.Type.WATCHING -> "Watching ${activity.name}"
            Activity.Type.CUSTOM -> {
                var prefix = ""
                if (activity.emoji.isPresent) {
                    val customEmoji = activity.emoji.get().asCustomEmoji()
                    if (customEmoji.isPresent)
                        prefix = this.emojiAsFormat(customEmoji.get()) + " "

                    val unicodeEmoji = activity.emoji.get().asUnicodeEmoji()
                    if (unicodeEmoji.isPresent)
                        prefix = unicodeEmoji.get().raw + " "
                }

                prefix + activity.state.orElse(activity.name)
            }
            else -> "Doing nothing"
        }
    }

    private fun emojiAsFormat(emoji: ReactionEmoji.Custom) : String {
        return if (emoji.isAnimated) {
            "<a:${emoji.name}:${emoji.id.asString()}>"
        } else {
            "<:${emoji.name}:${emoji.id.asString()}>"
        }
    }

    fun snowflakeAsDateTimeString(snowflake: Snowflake): String {
        val millis = snowflake.asLong() / 4194304 + 1420070400000L
        return formatDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC))
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(FORMATTER)
    }

    fun getAttachment(attachmentURL: String): InputStream {
        return try {
            val url = URL(attachmentURL)
            val connection = url.openConnection() as HttpsURLConnection
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
            connection.inputStream
        } catch (e: IOException) {
            object : InputStream() {
                override fun read(): Int {
                    return -1
                }
            }
        }
    }

    fun toHexString(color: Color): String {
        return String.format("#%02X%02X%02X", color.red, color.green, color.blue)
    }

}
