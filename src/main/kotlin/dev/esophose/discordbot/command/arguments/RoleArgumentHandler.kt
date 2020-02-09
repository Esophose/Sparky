package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class RoleArgumentHandler : DiscordCommandArgumentHandler<Role>() {

    override val handledType: KClass<Role>
        get() = Role::class

    override fun handleInternal(guild: Guild, input: String): Mono<Role> {
        return SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap { guild.getRoleById(it) }
                .switchIfEmpty(Mono.from(guild.roles.filter { x -> this.matchesRole(input, x) }))
    }

    private fun matchesRole(input: String, role: Role): Boolean {
        val name = role.name
        val mention = role.mention

        return input.equals(name, ignoreCase = true) || input.equals(mention, ignoreCase = true)
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid role: [$input]"
    }

}
