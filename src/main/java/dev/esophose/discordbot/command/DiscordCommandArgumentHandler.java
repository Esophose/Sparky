package dev.esophose.discordbot.command;

import discord4j.core.object.entity.Guild;
import java.util.Optional;
import reactor.core.publisher.Mono;

public abstract class DiscordCommandArgumentHandler<T> {

    @SuppressWarnings("unchecked")
    public Mono<Object> handle(Guild guild, String input, boolean isOptional) {
        if (isOptional) {
            return this.handleInternal(guild, input)
                    .map(Optional::of)
                    .cast(Object.class)
                    .switchIfEmpty(Mono.just(Optional.empty()));
        } else {
            return (Mono<Object>) this.handleInternal(guild, input);
        }
    }

    /**
     * Handles parsing a String argument to type T
     *
     * @param guild The guild the command was run in
     * @param input The input to parse
     * @return The parsed input result wrapped in a Mono
     */
    protected abstract Mono<T> handleInternal(Guild guild, String input);

    /**
     * Gets the error message to be displayed if this argument is invalid
     *
     * @param guild The guild the command was run in
     * @param input The input to parse
     * @return An error message indicating the given input is invalid
     */
    public abstract String getErrorMessage(Guild guild, String input);

    /**
     * @return the type of argument that is handled by this argument handler
     */
    public abstract Class<T> getHandledType();

    public Mono<Boolean> isInvalid(Guild guild, String input, boolean isOptional) {
        if (input.trim().isEmpty())
            return Mono.just(!isOptional);
        return this.handle(guild, input, false).hasElement().map(x -> !x);
    }

}
