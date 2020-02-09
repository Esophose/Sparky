package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import java.net.InetAddress;
import reactor.core.publisher.Mono;

public class InetAddressArgumentHandler extends DiscordCommandArgumentHandler<InetAddress> {

    @Override
    public Mono<InetAddress> handleInternal(Guild guild, String input) {
        if (input.trim().isEmpty())
            return Mono.empty();

        try {
            return Mono.just(InetAddress.getByName(input));
        } catch (Exception ex) {
            return Mono.empty();
        }
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Unknown host: [" + input + "]";
    }

    @Override
    public Class<InetAddress> getHandledType() {
        return InetAddress.class;
    }

}
