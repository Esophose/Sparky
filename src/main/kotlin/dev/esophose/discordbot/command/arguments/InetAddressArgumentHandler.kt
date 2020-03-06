package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import java.net.InetAddress
import kotlin.reflect.KClass

class InetAddressArgumentHandler : DiscordCommandArgumentHandler<InetAddress>() {

    override val handledType: KClass<InetAddress>
        get() = InetAddress::class

    public override fun handleInternal(guild: Guild, input: String): Mono<InetAddress> {
        if (input.trim { it <= ' ' }.isEmpty())
            return Mono.empty()

        return try {
            Mono.just(InetAddress.getByName(input))
        } catch (ex: Exception) {
            Mono.empty()
        }
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Unknown host: [$input]"
    }

}
