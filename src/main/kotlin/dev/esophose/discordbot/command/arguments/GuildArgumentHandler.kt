package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class GuildArgumentHandler : DiscordCommandArgumentHandler<Guild>() {

    override val handledType: KClass<Guild>
        get() = Guild::class

    override fun handleInternal(guild: Guild, input: String): Mono<Guild> {
        return SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap { Sparky.discord.getGuildById(it) }
                .switchIfEmpty(Mono.from(Sparky.discord.guilds.filter { x -> this.matchesName(input, x) }))
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid guild: [$input]"
    }

    private fun matchesName(input: String, guild: Guild): Boolean {
        val name = guild.name

        return input.equals(name, ignoreCase = true)
    }

}
