package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class SnowflakeArgumentHandler extends DiscordCommandArgumentHandler<Snowflake> {

    @Override
    protected Mono<Snowflake> handleInternal(Guild guild, String input) {
        try {
            long id = Long.parseUnsignedLong(input);
            return Mono.just(Snowflake.of(id));
        } catch (Exception ex) {
            return Mono.empty();
        }
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid snowflake: [" + input + "]";
    }

    @Override
    public Class<Snowflake> getHandledType() {
        return Snowflake.class;
    }

}
