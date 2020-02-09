package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class MemberArgumentHandler : DiscordCommandArgumentHandler<Member>() {

    override val handledType: KClass<Member>
        get() = Member::class

    override fun handleInternal(guild: Guild, input: String): Mono<Member> {
        return SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap { guild.getMemberById(it) }
                .switchIfEmpty(Mono.from(guild.members.filter { x -> this.matchesUsername(input, x) }))
    }

    private fun matchesUsername(input: String, member: Member): Boolean {
        val username = member.username
        val discriminator = member.discriminator
        val full = "$username#$discriminator"
        val mention = member.mention
        val nicknameMention = member.nicknameMention
        val displayName = member.displayName

        return (input.equals(username, ignoreCase = true)
                || input.equals(full, ignoreCase = true)
                || input.equals(displayName, ignoreCase = true)
                || input == mention
                || input == nicknameMention)
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid member: [$input]"
    }

}
