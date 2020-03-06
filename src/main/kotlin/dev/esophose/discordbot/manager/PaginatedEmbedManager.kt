package dev.esophose.discordbot.manager

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.misc.embed.PaginatedEmbed
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.ReactionAddEvent
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class PaginatedEmbedManager : Manager() {

    private val activeEmbeds: MutableMap<Snowflake, PaginatedEmbed>

    init {

        this.activeEmbeds = HashMap()
    }

    override fun enable() {
        Schedulers.parallel().schedulePeriodically({

        }, 5, 30, TimeUnit.SECONDS)
    }

    fun createPaginatedEmbed(creatorId: Snowflake, channelId: Snowflake, paginatedEmbedBuilder: (PaginatedEmbed) -> Unit): Mono<PaginatedEmbed> {
        val paginatedEmbed = PaginatedEmbed(creatorId)
        paginatedEmbedBuilder(paginatedEmbed)
        return paginatedEmbed.sendEmbedMessage(channelId)
                .doOnSuccess { message -> this.activeEmbeds[message.id] = paginatedEmbed }
                .thenReturn(paginatedEmbed)
    }

    fun changeEmbedState(event: ReactionAddEvent) {
        val messageId = event.messageId
        val embed = this.activeEmbeds[messageId]
        if (embed == null || event.userId == Sparky.self.id)
            return

        val emoji = event.emoji
        if (embed.creatorId != event.userId || !PaginatedEmbed.VALID_REACTIONS.contains(emoji)) {
            event.message.flatMap { x -> x.removeReaction(emoji, event.userId) }.subscribe()
            return
        }

        if (embed.changeEmbedState(emoji))
            this.activeEmbeds.remove(messageId)
    }

}
