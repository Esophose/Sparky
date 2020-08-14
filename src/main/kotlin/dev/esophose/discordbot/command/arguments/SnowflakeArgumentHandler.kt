package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class SnowflakeArgumentHandler : DiscordCommandArgumentHandler<Snowflake>() {

    override val handledType: KClass<Snowflake>
        get() = Snowflake::class

    public override fun handleInternal(guild: Guild, input: String): Mono<Snowflake> {
        return try {
            val id = java.lang.Long.parseUnsignedLong(input)
            Mono.just(Snowflake.of(id))
        } catch (ex: Exception) {
            Mono.empty()
        }
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid snowflake: [$input]"
    }

}
