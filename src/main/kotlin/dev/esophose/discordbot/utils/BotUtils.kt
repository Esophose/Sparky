package dev.esophose.discordbot.utils

import dev.esophose.discordbot.Sparky
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.rest.util.Color
import reactor.core.publisher.Mono
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

object BotUtils {

    private val FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")

    fun getWatchingUserCount(): Mono<Long> = Sparky.discord.guilds.flatMap { it.requestMembers() }.count()

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
            Activity.Type.COMPETING -> "Competing in ${activity.name}"
            Activity.Type.CUSTOM -> {
                var emoji = ""
                if (activity.emoji.isPresent) {
                    val customEmoji = activity.emoji.get().asCustomEmoji()
                    if (customEmoji.isPresent)
                        emoji = emojiAsFormat(customEmoji.get()) + " "

                    val unicodeEmoji = activity.emoji.get().asUnicodeEmoji()
                    if (unicodeEmoji.isPresent)
                        emoji = unicodeEmoji.get().raw + " "
                }

                emoji + activity.state.orElse(activity.name)
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

    fun sendLargeMessage(content: String, channel: TextChannel) {
        val messages = mutableListOf<String>()
        var messageChunk = content
        while (messageChunk.length > 1994) {
            messages.add(messageChunk.substring(0, 1994))
            messageChunk = messageChunk.substring(1994)
        }
        if (messageChunk.isNotEmpty())
            messages.add(messageChunk)
        for (message in messages)
            channel.createMessage("```$message```").subscribe()
    }

    fun round(number: Number): String {
        val decimalFormat = DecimalFormat("0.##")
        return decimalFormat.format(number)
    }

}
