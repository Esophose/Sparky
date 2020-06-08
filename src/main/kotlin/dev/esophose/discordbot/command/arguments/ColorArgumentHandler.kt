package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import discord4j.rest.util.Color
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class ColorArgumentHandler : DiscordCommandArgumentHandler<Color>() {

    override val handledType: KClass<Color>
        get() = Color::class

    override fun handleInternal(guild: Guild, input: String): Mono<Color> {
        return try {
            Mono.just(Color(java.awt.Color.decode(input).rgb))
        } catch (ex: Exception) {
            Mono.empty()
        }
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid color: [$input], make sure it starts with # if using a hex code"
    }

}
