package dev.esophose.discordbot.command

import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import java.util.*
import kotlin.reflect.KClass

abstract class DiscordCommandArgumentHandler<T : Any> {

    /**
     * @return the type of argument that is handled by this argument handler
     */
    abstract val handledType: KClass<T>

    @Suppress("UNCHECKED_CAST")
    fun handle(guild: Guild, input: String, isOptional: Boolean): Mono<Any> {
        return if (isOptional) {
            this.handleInternal(guild, input)
                    .map { Optional.of(it) }
                    .cast(Any::class.java)
                    .switchIfEmpty(Mono.just(Optional.empty<Any>()))
        } else {
            this.handleInternal(guild, input) as Mono<Any>
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
        return if (input.trim { it <= ' ' }.isEmpty()) Mono.just(!isOptional) else this.handle(guild, input, false).hasElement().map { x -> !x }
    }

}
