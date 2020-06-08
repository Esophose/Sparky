package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class UserArgumentHandler : DiscordCommandArgumentHandler<User>() {

    override val handledType: KClass<User>
        get() = User::class

    override fun handleInternal(guild: Guild, input: String): Mono<User> {
        return SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap { Sparky.discord.getUserById(it) }
                .switchIfEmpty(Mono.from(guild.members.filter { x -> this.matchesUsername(input, x) }))
    }

    private fun matchesUsername(input: String, member: User): Boolean {
        val username = member.username
        val discriminator = member.discriminator
        val full = "$username#$discriminator"
        val mention = member.mention

        return (input.equals(username, ignoreCase = true)
                || input.equals(full, ignoreCase = true)
                || input == mention)
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid user: [$input]"
    }

}
