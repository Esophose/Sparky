package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.PaginatedEmbedManager
import discord4j.core.event.domain.message.ReactionAddEvent

class PaginatedEmbedListener : Listener<ReactionAddEvent>(ReactionAddEvent::class.java) {

    private var paginatedEmbedManager: PaginatedEmbedManager? = null

    override fun execute(event: ReactionAddEvent) {
        if (this.paginatedEmbedManager == null)
            this.paginatedEmbedManager = Sparky.getManager(PaginatedEmbedManager::class)

        this.paginatedEmbedManager!!.changeEmbedState(event)
    }

}
