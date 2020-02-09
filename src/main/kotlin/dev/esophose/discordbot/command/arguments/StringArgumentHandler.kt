package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class StringArgumentHandler : DiscordCommandArgumentHandler<String>() {

    override val handledType: KClass<String>
        get() = String::class

    override fun handleInternal(guild: Guild, input: String): Mono<String> {
        return if (input.isBlank()) Mono.empty() else Mono.just(input)
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid string, cannot be empty"
    }

}
