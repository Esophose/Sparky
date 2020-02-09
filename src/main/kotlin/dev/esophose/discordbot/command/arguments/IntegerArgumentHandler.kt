package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class IntegerArgumentHandler : DiscordCommandArgumentHandler<Int>() {

    override val handledType: KClass<Int>
        get() = Int::class

    override fun handleInternal(guild: Guild, input: String): Mono<Int> {
        return try {
            Mono.just(Integer.parseInt(input))
        } catch (e: Exception) {
            Mono.empty()
        }
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid integer: [$input]"
    }

}
