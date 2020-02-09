package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import java.awt.Color;
import reactor.core.publisher.Mono;

public class ColorArgumentHandler extends DiscordCommandArgumentHandler<Color> {

    @Override
    protected Mono<Color> handleInternal(Guild guild, String input) {
        try {
            return Mono.just(Color.decode(input));
        } catch (Exception ex) {
            return Mono.empty();
        }
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid color: [" + input + "], make sure it starts with # if using a hex code";
    }

    @Override
    public Class<Color> getHandledType() {
        return Color.class;
    }

}
