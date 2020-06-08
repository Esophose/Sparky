package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import discord4j.core.event.domain.message.MessageBulkDeleteEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.MessageDeleteEvent
import discord4j.core.event.domain.message.MessageEvent
import discord4j.core.event.domain.message.MessageUpdateEvent
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MessageAuditListener : Listener<MessageEvent>(MessageCreateEvent::class, MessageUpdateEvent::class, MessageDeleteEvent::class, MessageBulkDeleteEvent::class) {

    override fun execute(event: MessageEvent) {
        when (event) {
            is MessageCreateEvent -> executeCreate(event)
            is MessageUpdateEvent -> executeUpdate(event)
            is MessageDeleteEvent -> executeDelete(event)
            is MessageBulkDeleteEvent -> executeBulkDelete(event)
        }
    }

    private fun executeCreate(event: MessageCreateEvent) {
        if (event.guildId.isEmpty || event.member.isEmpty)
            return

        Sparky.connector.connect { connection ->
            val insert = "INSERT INTO message_audit (guild_id, channel_id, author_id, message_id, datetime, content, type) VALUES (?, ?, ?, ?, ?, ?, ?)"
            connection.prepareStatement(insert).use {
                it.run {
                    setLong(1, event.guildId.get().asLong())
                    setLong(2, event.message.channelId.asLong())
                    setLong(3, event.member.get().id.asLong())
                    setLong(4, event.message.id.asLong())
                    setString(5, DateTimeFormatter.ISO_INSTANT.format(event.message.timestamp))
                    setString(6, event.message.content)
                    setString(7, "create")
                    executeUpdate()
                }
            }
        }
    }

    private fun executeUpdate(event: MessageUpdateEvent) {
        event.message.subscribe { message ->
            if (!event.isContentChanged || event.currentContent.isEmpty || event.guildId.isEmpty || message.author.isEmpty || message.editedTimestamp.isEmpty)
                return@subscribe

            Sparky.connector.connect { connection ->
                val insert = "INSERT INTO message_audit (guild_id, channel_id, author_id, message_id, datetime, content, type) VALUES (?, ?, ?, ?, ?, ?, ?)"
                connection.prepareStatement(insert).use {
                    it.run {
                        setLong(1, event.guildId.get().asLong())
                        setLong(2, event.channelId.asLong())
                        setLong(3, message.author.get().id.asLong())
                        setLong(4, event.messageId.asLong())
                        setString(5, DateTimeFormatter.ISO_INSTANT.format(message.editedTimestamp.get()))
                        setString(6, event.currentContent.get())
                        setString(7, "edit")
                        executeUpdate()
                    }
                }
            }
        }
    }

    private fun executeDelete(event: MessageDeleteEvent) {
        if (event.message.isEmpty)
            return

        val message = event.message.get()
        message.guild.subscribe { guild ->
            if (message.author.isEmpty)
                return@subscribe

            Sparky.connector.connect { connection ->
                val insert = "INSERT INTO message_audit (guild_id, channel_id, author_id, message_id, datetime, content, type) VALUES (?, ?, ?, ?, ?, ?, ?)"
                connection.prepareStatement(insert).use {
                    it.run {
                        setLong(1, guild.id.asLong())
                        setLong(2, event.channelId.asLong())
                        setLong(3, message.author.get().id.asLong())
                        setLong(4, event.messageId.asLong())
                        setString(5, DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.MILLIS)))
                        setString(6, message.content)
                        setString(7, "delete")
                        executeUpdate()
                    }
                }
            }
        }
    }

    private fun executeBulkDelete(event: MessageBulkDeleteEvent) {
        Sparky.connector.connect { connection ->
            val insert = "INSERT INTO message_audit (guild_id, channel_id, author_id, message_id, datetime, content, type) VALUES (?, ?, ?, ?, ?, ?, ?)"
            connection.prepareStatement(insert).use {
                for (message in event.messages) {
                    if (message.author.isEmpty)
                        continue

                    it.run {
                        setLong(1, event.guildId.asLong())
                        setLong(2, event.channelId.asLong())
                        setLong(3, message.author.get().id.asLong())
                        setLong(4, message.id.asLong())
                        setString(5, DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.MILLIS)))
                        setString(6, message.content)
                        setString(7, "delete")
                        addBatch()
                    }
                }
                it.executeBatch()
            }
        }
    }

}

