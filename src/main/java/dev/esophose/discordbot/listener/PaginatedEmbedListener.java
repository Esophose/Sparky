package dev.esophose.discordbot.listener;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.manager.PaginatedEmbedManager;
import discord4j.core.event.domain.message.ReactionAddEvent;

public class PaginatedEmbedListener extends Listener<ReactionAddEvent> {

    private PaginatedEmbedManager paginatedEmbedManager;

    public PaginatedEmbedListener() {
        super(ReactionAddEvent.class);
    }

    @Override
    public void execute(ReactionAddEvent event) {
        if (this.paginatedEmbedManager == null)
            this.paginatedEmbedManager = Sparky.getInstance().getManager(PaginatedEmbedManager.class);

        this.paginatedEmbedManager.changeEmbedState(event);
    }

}
