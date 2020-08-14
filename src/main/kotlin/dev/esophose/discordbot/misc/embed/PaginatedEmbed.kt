package dev.esophose.discordbot.misc.embed

import dev.esophose.discordbot.Sparky
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Mono
import java.util.ArrayList

class PaginatedEmbed(val creatorId: Snowflake) {

    private var channelId: Snowflake? = null
    private var messageId: Snowflake? = null
    private val embeds: MutableList<EmbedStorage>
    private var currentPageIndex: Int = 0

    val currentPageNumber: Int
        get() = this.currentPageIndex + 1

    val maxPageNumber: Int
        get() = this.embeds.size

    val maxPageIndex: Int
        get() = this.embeds.size - 1

    val embedMessage: Mono<Message>
        get() = this.channel.flatMap { channel -> channel.getMessageById(this.messageId) }

    private val channel: Mono<TextChannel>
        get() = Sparky.discord
                .getChannelById(this.channelId!!)
                .cast(TextChannel::class.java)

    init {
        this.embeds = ArrayList()
        this.currentPageIndex = 0
    }

    fun addPage(embedStorage: (EmbedStorage) -> Unit) {
        val newEmbedStorage = EmbedStorage()
        embedStorage(newEmbedStorage)
        this.embeds.add(newEmbedStorage)
    }

    fun nextPage(): Mono<Message> {
        if (!this.hasNextPage())
            throw IllegalStateException("No forward pages")

        this.currentPageIndex++
        return this.updateEmbedMessage()
    }

    fun previousPage(): Mono<Message> {
        if (!this.hasPreviousPage())
            throw IllegalStateException("No previous pages")

        this.currentPageIndex--
        return this.updateEmbedMessage()
    }

    fun lastPage(): Mono<Message> {
        this.currentPageIndex = this.maxPageIndex
        return this.updateEmbedMessage()
    }

    fun firstPage(): Mono<Message> {
        this.currentPageIndex = 0
        return this.updateEmbedMessage()
    }

    fun hasNextPage(): Boolean {
        return this.currentPageIndex < this.embeds.size - 1
    }

    fun hasMultipleNextPages(): Boolean {
        return this.currentPageIndex < this.embeds.size - 2
    }

    fun hasPreviousPage(): Boolean {
        return this.currentPageIndex > 0
    }

    fun hasMultiplePreviousPages(): Boolean {
        return this.currentPageIndex > 1
    }

    fun sendEmbedMessage(channelId: Snowflake): Mono<Message> {
        this.channelId = channelId
        return this.channel
                .flatMap { channel -> channel.createEmbed { this.applyEmbedCreateSpec(it) } }
                .doOnSuccess { message ->
                    this.messageId = message.id
                    this.updateEmbedReactions(message)
                }
    }

    fun changeEmbedState(emoji: ReactionEmoji): Boolean {
        when (emoji) {
            FIRST_ARROW -> this.firstPage().subscribe()
            BACK_ARROW -> this.previousPage().subscribe()
            STOP -> {
                this.embedMessage.flatMap { it.removeAllReactions() }.subscribe()
                return true
            }
            FORWARD_ARROW -> this.nextPage().subscribe()
            LAST_ARROW -> this.lastPage().subscribe()
        }

        return false
    }

    private fun updateEmbedMessage(): Mono<Message> {
        return this.embedMessage
                .flatMap { x -> x.edit { messageEditSpec -> messageEditSpec.setEmbed { this.applyEmbedCreateSpec(it) } } }
                .doOnSuccess { this.updateEmbedReactions(it) }
    }

    private fun updateEmbedReactions(message: Message) {
        message.removeAllReactions()
                .then(this.getReactionsMono(message))
                .subscribe()
    }

    private fun getReactionsMono(message: Message): Mono<Void> {
        if (this.embeds.size == 1)
            return Mono.empty()

        var addReactionsMono: Mono<Void>? = null
        if (this.hasMultiplePreviousPages())
            addReactionsMono = message.addReaction(FIRST_ARROW)

        if (this.hasPreviousPage()) {
            addReactionsMono = if (addReactionsMono == null) {
                message.addReaction(BACK_ARROW)
            } else {
                addReactionsMono.then(message.addReaction(BACK_ARROW))
            }
        }

        addReactionsMono = if (addReactionsMono == null) {
            message.addReaction(STOP)
        } else {
            addReactionsMono.then(message.addReaction(STOP))
        }

        if (this.hasNextPage())
            addReactionsMono = addReactionsMono!!.then(message.addReaction(FORWARD_ARROW))

        if (this.hasMultipleNextPages())
            addReactionsMono = addReactionsMono!!.then(message.addReaction(LAST_ARROW))

        return addReactionsMono
    }

    private fun applyEmbedCreateSpec(embedCreateSpec: EmbedCreateSpec) {
        if (this.embeds.isEmpty())
            throw IllegalStateException("Must have at least one embed added")

        this.embeds[this.currentPageIndex].applyToCreateSpec(embedCreateSpec, this.currentPageNumber, this.maxPageNumber)
    }

    companion object {
        private val FIRST_ARROW = ReactionEmoji.unicode("\u23EA")
        private val BACK_ARROW = ReactionEmoji.unicode("\u2B05\uFE0F")
        private val STOP = ReactionEmoji.unicode("\u23F9\uFE0F")
        private val FORWARD_ARROW = ReactionEmoji.unicode("\u27A1\uFE0F")
        private val LAST_ARROW = ReactionEmoji.unicode("\u23E9")
        val VALID_REACTIONS = listOf(FIRST_ARROW, BACK_ARROW, STOP, FORWARD_ARROW, LAST_ARROW)
    }

}
