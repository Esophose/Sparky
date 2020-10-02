package dev.esophose.discordbot.command

import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.Optional
import kotlin.reflect.KClass

abstract class DiscordCommandArgumentHandler<T : Any> {

    /**
     * @return the type of argument that is handled by this argument handler
     */
    abstract val handledType: KClass<T>

    @Suppress("UNCHECKED_CAST")
    fun handle(guild: Guild, input: String, isOptional: Boolean, position: Int): Mono<Tuple2<Int, Any>> {
        return if (isOptional) {
            this.handleInternal(guild, input)
                    .map { Optional.of(it) }
                    .cast(Any::class.java)
                    .switchIfEmpty(Mono.just(Optional.empty<Any>()))
                    .map { Tuples.of(position, it) }
        } else {
            (this.handleInternal(guild, input) as Mono<Any>).map { Tuples.of(position, it) }
        }
    }

    /**
     * Handles parsing a String argument to type T
     *
     * @param guild The guild the command was run in
     * @param input The input to parse
     * @return The parsed input result wrapped in a Mono
     */
    protected abstract fun handleInternal(guild: Guild, input: String): Mono<T>

    /**
     * Gets the error message to be displayed if this argument is invalid
     *
     * @param guild The guild the command was run in
     * @param input The input to parse
     * @return An error message indicating the given input is invalid
     */
    abstract fun getErrorMessage(guild: Guild, input: String): String

    fun isInvalid(guild: Guild, input: String, isOptional: Boolean): Mono<Boolean> {
        return if (input.trim { it <= ' ' }.isEmpty()) Mono.just(!isOptional) else this.handle(guild, input, false, -1).hasElement().map { x -> !x }
    }

}
