package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class StringArgumentHandler extends DiscordCommandArgumentHandler<String> {

    @Override
    protected Mono<String> handleInternal(Guild guild, String input) {
        if (input.isBlank())
            return Mono.empty();
        return Mono.just(input);
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid string, cannot be empty";
    }

    @Override
    public Class<String> getHandledType() {
        return String.class;
    }

}
