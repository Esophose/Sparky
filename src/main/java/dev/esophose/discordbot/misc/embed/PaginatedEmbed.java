package dev.esophose.discordbot.misc.embed;

import dev.esophose.discordbot.Sparky;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;

public class PaginatedEmbed {

    private static final ReactionEmoji FIRST_ARROW = ReactionEmoji.unicode("\u23EA");
    private static final ReactionEmoji BACK_ARROW = ReactionEmoji.unicode("\u2B05\uFE0F");
    private static final ReactionEmoji STOP = ReactionEmoji.unicode("\u23F9\uFE0F");
    private static final ReactionEmoji FORWARD_ARROW = ReactionEmoji.unicode("\u27A1\uFE0F");
    private static final ReactionEmoji LAST_ARROW = ReactionEmoji.unicode("\u23E9");
    public static final List<ReactionEmoji> VALID_REACTIONS = Arrays.asList(FIRST_ARROW, BACK_ARROW, STOP, FORWARD_ARROW, LAST_ARROW);

    private Sparky bot;
    private Snowflake creatorId;
    private Snowflake channelId;
    private Snowflake messageId;
    private List<EmbedStorage> embeds;
    private int currentPageIndex;

    public PaginatedEmbed(Snowflake creatorId) {
        this.bot = Sparky.getInstance();
        this.creatorId = creatorId;
        this.embeds = new ArrayList<>();
        this.currentPageIndex = 0;
    }

    public Snowflake getCreatorId() {
        return this.creatorId;
    }

    public void addPage(Consumer<EmbedStorage> embedStorage) {
        EmbedStorage newEmbedStorage = new EmbedStorage();
        embedStorage.accept(newEmbedStorage);
        this.embeds.add(newEmbedStorage);
    }

    public int getCurrentPageNumber() {
        return this.currentPageIndex + 1;
    }

    public int getMaxPageNumber() {
        return this.embeds.size();
    }

    public int getMaxPageIndex() {
        return this.embeds.size() - 1;
    }

    public Mono<Message> nextPage() {
        if (!this.hasNextPage())
            throw new IllegalStateException("No forward pages");

        this.currentPageIndex++;
        return this.updateEmbedMessage();
    }

    public Mono<Message> previousPage() {
        if (!this.hasPreviousPage())
            throw new IllegalStateException("No previous pages");

        this.currentPageIndex--;
        return this.updateEmbedMessage();
    }

    public Mono<Message> lastPage() {
        this.currentPageIndex = this.getMaxPageIndex();
        return this.updateEmbedMessage();
    }

    public Mono<Message> firstPage() {
        this.currentPageIndex = 0;
        return this.updateEmbedMessage();
    }

    public boolean hasNextPage() {
        return this.currentPageIndex < this.embeds.size() - 1;
    }

    public boolean hasMultipleNextPages() {
        return this.currentPageIndex < this.embeds.size() - 2;
    }

    public boolean hasPreviousPage() {
        return this.currentPageIndex > 0;
    }

    public boolean hasMultiplePreviousPages() {
        return this.currentPageIndex > 1;
    }

    public Mono<Message> getEmbedMessage() {
        return this.getChannel().flatMap(channel -> channel.getMessageById(this.messageId));
    }

    public Mono<Message> sendEmbedMessage(Snowflake channelId) {
        this.channelId = channelId;
        return this.getChannel()
                .flatMap(channel -> channel.createEmbed(this::applyEmbedCreateSpec))
                .doOnSuccess(message -> {
                    this.messageId = message.getId();
                    this.updateEmbedReactions(message);
                });
    }

    public boolean changeEmbedState(ReactionEmoji emoji) {
        if (emoji.equals(FIRST_ARROW)) {
            this.firstPage().subscribe();
        } else if (emoji.equals(BACK_ARROW)) {
            this.previousPage().subscribe();
        } else if (emoji.equals(STOP)) {
            this.getEmbedMessage().flatMap(Message::removeAllReactions).subscribe();
            return true;
        } else if (emoji.equals(FORWARD_ARROW)) {
            this.nextPage().subscribe();
        } else if (emoji.equals(LAST_ARROW)) {
            this.lastPage().subscribe();
        }

        return false;
    }

    private Mono<Message> updateEmbedMessage() {
        return this.getEmbedMessage()
                .flatMap(x -> x.edit(messageEditSpec -> messageEditSpec.setEmbed(this::applyEmbedCreateSpec)))
                .doOnSuccess(this::updateEmbedReactions);
    }

    private void updateEmbedReactions(Message message) {
        message.removeAllReactions()
                .then(this.getReactionsMono(message))
                .subscribe();
    }

    private Mono<Void> getReactionsMono(Message message) {
        if (this.embeds.size() == 1)
            return Mono.empty();

        Mono<Void> addReactionsMono = null;
        if (this.hasMultiplePreviousPages())
            addReactionsMono = message.addReaction(FIRST_ARROW);

        if (this.hasPreviousPage()) {
            if (addReactionsMono == null) {
                addReactionsMono = message.addReaction(BACK_ARROW);
            } else {
                addReactionsMono = addReactionsMono.then(message.addReaction(BACK_ARROW));
            }
        }

        if (addReactionsMono == null) {
            addReactionsMono = message.addReaction(STOP);
        } else {
            addReactionsMono = addReactionsMono.then(message.addReaction(STOP));
        }

        if (this.hasNextPage())
            addReactionsMono = addReactionsMono.then(message.addReaction(FORWARD_ARROW));

        if (this.hasMultipleNextPages())
            addReactionsMono = addReactionsMono.then(message.addReaction(LAST_ARROW));

        return addReactionsMono;
    }

    private void applyEmbedCreateSpec(EmbedCreateSpec embedCreateSpec) {
        if (this.embeds.isEmpty())
            throw new IllegalStateException("Must have at least one embed added");

        this.embeds.get(this.currentPageIndex).applyToCreateSpec(embedCreateSpec, this.getCurrentPageNumber(), this.getMaxPageNumber());
    }

    private Mono<TextChannel> getChannel() {
        return this.bot.getDiscord()
                .getChannelById(this.channelId)
                .cast(TextChannel.class);
    }

}
