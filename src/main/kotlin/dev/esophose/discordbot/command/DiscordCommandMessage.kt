package dev.esophose.discordbot.command

import dev.esophose.discordbot.Sparky
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import reactor.core.publisher.Mono

class DiscordCommandMessage(val guildId: Snowflake, val channelId: Snowflake, val messageId: Snowflake, val authorId: Snowflake) {

    val content: Mono<String>
        get() = this.actualMessage.map { it.content }

    val actualMessage: Mono<Message>
        get() = this.channel.flatMap { channel -> channel.getMessageById(this.messageId) }

    val channel: Mono<MessageChannel>
        get() = this.guild
                .flatMap { guild -> guild.getChannelById(this.channelId) }
                .cast(MessageChannel::class.java)

    val guild: Mono<Guild>
        get() = Sparky.discord.getGuildById(this.guildId)

    /**
     * @return the id of the author of the command. This is not necessarily the same Member that created the message.
     */
    val author: Mono<Member>
        get() = this.guild.flatMap { guild -> guild.getMemberById(this.authorId) }

    fun delete(): Mono<Void> = this.actualMessage.flatMap { it.delete() }

}
