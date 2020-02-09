package dev.esophose.discordbot.command;

import dev.esophose.discordbot.Sparky;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.util.Snowflake;
import java.util.Optional;
import reactor.core.publisher.Mono;

public class DiscordCommandMessage {

    private final Snowflake guildId;
    private final Snowflake channelId;
    private final Snowflake authorId;
    private Snowflake messageId;

    public DiscordCommandMessage(Snowflake guildId, Snowflake channelId, Snowflake messageId, Snowflake authorId) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.authorId = authorId;
    }

    public Snowflake getGuildId() {
        return this.guildId;
    }

    public Snowflake getChannelId() {
        return this.channelId;
    }

    public Snowflake getMessageId() {
        return this.messageId;
    }

    public Snowflake getAuthorId() {
        return this.authorId;
    }

    public Mono<Void> delete() {
        return this.getActualMessage().flatMap(Message::delete);
    }

    public Mono<Optional<String>> getContent() {
        return this.getActualMessage().map(Message::getContent);
    }

    public Mono<Message> getActualMessage() {
        return this.getChannel().flatMap(channel -> channel.getMessageById(this.messageId));
    }

    public Mono<MessageChannel> getChannel() {
        return this.getGuild()
                .flatMap(guild -> guild.getChannelById(this.channelId))
                .cast(MessageChannel.class);
    }

    public Mono<Guild> getGuild() {
        return Sparky.getInstance().getDiscord().getGuildById(this.guildId);
    }

    /**
     * @return the id of the author of the command. This is not necessarily the same Member that created the message.
     */
    public Mono<Member> getAuthor() {
        return this.getGuild().flatMap(guild -> guild.getMemberById(this.authorId));
    }

}
