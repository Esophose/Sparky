package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet

class MessageCreateSecurityListener : Listener<MessageCreateEvent>(MessageCreateEvent::class) {

    private val auditedEvents: MutableMap<TextChannel, MutableList<AuditedEvent>> = Collections.synchronizedMap(HashMap())
    private val dangerModes: MutableSet<TextChannel> = Collections.synchronizedSet(HashSet())

    override fun execute(event: MessageCreateEvent) {
        if (event.member.isEmpty || event.member.get().isBot)
            return

        Mono.zip(event.guild, event.message.channel).subscribe { monos ->
            if (monos.t2 !is TextChannel) {
                Mono.zip(event.guild, event.message.channel).subscribe()
                return@subscribe
            }

            val commandManager = Sparky.getManager(CommandManager::class)
            val guild = monos.t1
            val channel = monos.t2 as TextChannel

            // Audit the new message
            val events = this.auditedEvents.computeIfAbsent(channel) { ArrayList() }
            events.add(AuditedEvent())

            // Prune old entries
            events.removeIf { it.shouldPrune() }
            if (events.isEmpty())
                this.auditedEvents.remove(channel)

            // Are we in danger?
            if (events.size >= DANGER_THRESHOLD && !this.dangerModes.contains(channel)) {
                this.dangerModes.add(channel)
                channel.edit { spec -> spec.setRateLimitPerUser(MESSAGE_RATE_LIMIT) }.subscribe()
                commandManager.sendResponse(Mono.just(channel), "\u26a0 Rate Limited", "This channel has been rate limited due to a sudden influx of messages.\nThis rate limit will automatically expire in 10 minutes.").subscribe()

                // Get guild owner and send a DM
                guild.owner.flatMap { it.privateChannel }.flatMap { x ->
                    x.createMessage { spec ->
                        spec.setEmbed { embedSpec ->
                            commandManager.applyEmbedSpec(guild.id, embedSpec, "\u26a0 Your guild might be in danger!",
                                    "A large influx of messages have been posted in ${channel.mention} in ${guild.name} within the past 10 seconds.\n" +
                                            "We've temporarily put the channel in slowmode for the next 10 minutes as a precaution.", null)
                        }
                    }
                }.subscribe()

                // Expire automatically after a while
                Mono.delay(Duration.ofMillis(DANGER_UPDATE_THRESHOLD.toLong())).subscribe {
                    channel.edit { spec -> spec.setRateLimitPerUser(0) }.subscribe()
                    this.dangerModes.remove(channel)
                    commandManager.sendResponse(Mono.just(channel), "\u26a0 Rate Limit Removed", "The temporary rate limit has been removed from this channel.\nPlease avoid spamming the chat.").subscribe()
                }
            }
        }
    }

    private class AuditedEvent {
        private val auditTime: Long = System.currentTimeMillis()

        fun shouldPrune(): Boolean {
            return System.currentTimeMillis() - this.auditTime > PRUNE_TIME
        }
    }

    companion object {
        private const val DANGER_THRESHOLD = 30 // 30 messages
        private const val PRUNE_TIME = 1000 * 10 // 10 seconds
        private const val DANGER_UPDATE_THRESHOLD = 1000 * 60 * 10 // 10 minutes
        private const val MESSAGE_RATE_LIMIT = 30 // 30 seconds
    }

}
