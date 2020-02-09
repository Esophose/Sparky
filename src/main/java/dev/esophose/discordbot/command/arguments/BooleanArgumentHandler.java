package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class BooleanArgumentHandler extends DiscordCommandArgumentHandler<Boolean> {

    @Override
    protected Mono<Boolean> handleInternal(Guild guild, String input) {
        try {
            return Mono.just(Boolean.parseBoolean(input));
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid boolean: [" + input + "]";
    }

    @Override
    public Class<Boolean> getHandledType() {
        return Boolean.class;
    }

}
