package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.guild.MemberJoinEvent
import reactor.core.publisher.Flux
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

class MemberJoinSecurityListener : Listener<MemberJoinEvent>(MemberJoinEvent::class) {

    private val auditedEvents: MutableMap<Guild, MutableList<AuditedEvent>> = Collections.synchronizedMap(HashMap())
    private val dangerModes: MutableMap<Guild, Long> = Collections.synchronizedMap(HashMap())

    override fun execute(event: MemberJoinEvent) {
        event.guild.subscribe { guild ->
            val member = event.member
            val commandManager = Sparky.getManager(CommandManager::class)

            // If we're in danger, check if we should continue
            if (this.dangerModes.containsKey(guild)) {
                val lastDangerUpdate = this.dangerModes[guild]!!
                if (System.currentTimeMillis() - lastDangerUpdate > DANGER_UPDATE_THRESHOLD) {
                    this.dangerModes.remove(guild)
                } else {
                    member.kick().subscribe()
                    this.dangerModes[guild] = System.currentTimeMillis()
                    return@subscribe
                }
            }

            // Audit the new member joining
            val events = this.auditedEvents.computeIfAbsent(guild) { ArrayList() }
            events.add(AuditedEvent(member))

            // Prune old entries
            events.removeIf { it.shouldPrune() }
            if (events.isEmpty())
                this.auditedEvents.remove(guild)

            // Are we in danger?
            if (events.size >= DANGER_THRESHOLD && !this.dangerModes.containsKey(guild)) {
                this.dangerModes[guild] = System.currentTimeMillis()
                Flux.fromIterable(events).map { it.member }.flatMap { it.kick() }.subscribe()
                this.auditedEvents.remove(guild)
                // Get guild owner and send a DM
                guild.owner.flatMap { it.privateChannel }.flatMap { x ->
                    x.createMessage { spec ->
                        spec.setEmbed { embedSpec ->
                            commandManager.applyEmbedSpec(guild.id, embedSpec, "\u26a0 Your guild might be in danger!",
                                    "A large influx of users have joined " + guild.name + " within the past minute.\n" +
                                            "We've temporarily disabled allowing members to join for the next 3 minutes or as long as this potential danger continues.", null)
                        }
                    }
                }.subscribe()
            }
        }
    }

    private class AuditedEvent(val member: Member) {
        private val auditTime = System.currentTimeMillis()

        fun shouldPrune(): Boolean {
            return System.currentTimeMillis() - this.auditTime > PRUNE_TIME
        }
    }

    companion object {
        private const val DANGER_THRESHOLD = 10 // 10 joins
        private const val PRUNE_TIME = 1000 * 30 // 30 seconds
        private const val DANGER_UPDATE_THRESHOLD = 1000 * 60 * 3 // 3 minutes
    }

}
