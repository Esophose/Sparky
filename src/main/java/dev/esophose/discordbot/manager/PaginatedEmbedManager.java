package dev.esophose.discordbot.manager;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.misc.embed.PaginatedEmbed;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class PaginatedEmbedManager extends Manager {

    private Map<Snowflake, PaginatedEmbed> activeEmbeds;

    public PaginatedEmbedManager(Sparky bot) {
        super(bot);

        this.activeEmbeds = new HashMap<>();
    }

    @Override
    public void enable() {
        Schedulers.parallel().schedulePeriodically(() -> {

        }, 5, 30, TimeUnit.SECONDS);
    }

    public Mono<PaginatedEmbed> createPaginatedEmbed(Snowflake creatorId, Snowflake channelId, Consumer<PaginatedEmbed> paginatedEmbedBuilder) {
        PaginatedEmbed paginatedEmbed = new PaginatedEmbed(creatorId);
        paginatedEmbedBuilder.accept(paginatedEmbed);
        return paginatedEmbed.sendEmbedMessage(channelId)
                .doOnSuccess(message -> this.activeEmbeds.put(message.getId(), paginatedEmbed))
                .thenReturn(paginatedEmbed);
    }

    public void changeEmbedState(ReactionAddEvent event) {
        Snowflake messageId = event.getMessageId();
        PaginatedEmbed embed = this.activeEmbeds.get(messageId);
        if (embed == null || event.getUserId().equals(this.bot.getSelf().getId()))
            return;

        ReactionEmoji emoji = event.getEmoji();
        if (!embed.getCreatorId().equals(event.getUserId()) || !PaginatedEmbed.VALID_REACTIONS.contains(emoji)) {
            event.getMessage().flatMap(x -> x.removeReaction(emoji, event.getUserId())).subscribe();
            return;
        }

        if (embed.changeEmbedState(emoji))
            this.activeEmbeds.remove(messageId);
    }

}
