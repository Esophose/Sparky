package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class IntegerArgumentHandler extends DiscordCommandArgumentHandler<Integer> {

    @Override
    protected Mono<Integer> handleInternal(Guild guild, String input) {
        try {
            return Mono.just(Integer.parseInt(input));
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid integer: [" + input + "]";
    }

    @Override
    public Class<Integer> getHandledType() {
        return Integer.class;
    }

}
