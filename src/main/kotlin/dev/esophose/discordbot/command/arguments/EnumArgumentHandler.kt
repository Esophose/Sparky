package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.reflect.KClass

class EnumArgumentHandler<T : Enum<T>> : DiscordCommandArgumentHandler<T>() {

    /**
     * Must be set before running handleInternal() in order to parse the enum properly
     */
    var currentHandledType: KClass<*>? = null

    @Suppress("UNCHECKED_CAST")
    override val handledType: KClass<T>
        get() {
            return if (this.currentHandledType == null) {
                Enum::class as KClass<T>
            } else {
                currentHandledType as KClass<T>
            }
        }

    override fun handleInternal(guild: Guild, input: String): Mono<T> {
        val match = Stream.of(*this.handledType.java.enumConstants)
                .filter { x -> x.name.equals(input, ignoreCase = true) }
                .findFirst()

        return match.map { Mono.just(it) }.orElse(Mono.empty())
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid ${this.handledType.simpleName} type [$input]. Valid types: " +
                Stream.of(*this.handledType.java.enumConstants).map { it.name.toLowerCase() }.collect(Collectors.joining(", "))
    }

}
