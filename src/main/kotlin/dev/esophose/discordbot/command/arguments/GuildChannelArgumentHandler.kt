package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.channel.GuildChannel
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class GuildChannelArgumentHandler : DiscordCommandArgumentHandler<GuildChannel>() {

    override val handledType: KClass<GuildChannel>
        get() = GuildChannel::class

    override fun handleInternal(guild: Guild, input: String): Mono<GuildChannel> {
        return SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap { guild.getChannelById(it) }
                .switchIfEmpty(Mono.from(guild.channels.filter { x -> this.matchesName(input, x) }))
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid channel: [$input]"
    }

    private fun matchesName(input: String, channel: GuildChannel): Boolean {
        val name = channel.name
        val mention = channel.mention

        return input.equals(name, ignoreCase = true) || input == mention
    }

}
