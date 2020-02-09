package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

public class EnumArgumentHandler<T extends Enum<T>> extends DiscordCommandArgumentHandler<T> {

    private Class<T> handledType;

    @Override
    protected Mono<T> handleInternal(Guild guild, String input) {
        Optional<T> match = Stream.of(this.getHandledType().getEnumConstants())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .findFirst();

        return match.map(Mono::just).orElse(Mono.empty());
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid " + this.handledType.getSimpleName() + " type [" + input + "]. Valid types: " +
                Stream.of(this.getHandledType().getEnumConstants()).map(x -> x.name().toLowerCase()).collect(Collectors.joining(", "));
    }

    @Override
    public Class<T> getHandledType() {
        return this.handledType;
    }

    /**
     * Must be called before running handleInternal() in order to parse the enum properly
     *
     * @param handledType The enum type to handle
     */
    public void setHandledType(Class<T> handledType) {
        this.handledType = handledType;
    }

}
