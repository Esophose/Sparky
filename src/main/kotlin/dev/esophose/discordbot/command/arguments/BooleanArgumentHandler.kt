package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class BooleanArgumentHandler : DiscordCommandArgumentHandler<Boolean>() {

    override val handledType: KClass<Boolean>
        get() = Boolean::class

    override fun handleInternal(guild: Guild, input: String): Mono<Boolean> {
        return try {
            Mono.just(java.lang.Boolean.parseBoolean(input))
        } catch (e: Exception) {
            Mono.empty()
        }
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid boolean: [$input]"
    }

}
