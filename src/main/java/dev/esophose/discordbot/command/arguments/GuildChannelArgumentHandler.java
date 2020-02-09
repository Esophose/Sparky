package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import reactor.core.publisher.Mono;

public class GuildChannelArgumentHandler extends DiscordCommandArgumentHandler<GuildChannel> {

    @Override
    protected Mono<GuildChannel> handleInternal(Guild guild, String input) {
        return new SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap(guild::getChannelById)
                .switchIfEmpty(Mono.from(guild.getChannels().filter(x -> this.matchesName(input, x))));
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid channel: [" + input + "]";
    }

    private boolean matchesName(String input, GuildChannel channel) {
        String name = channel.getName();
        String mention = channel.getMention();

        return input.equalsIgnoreCase(name)
            || input.equals(mention);
    }

    @Override
    public Class<GuildChannel> getHandledType() {
        return GuildChannel.class;
    }

}
