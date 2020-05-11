package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.channel.Category
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import org.apache.commons.lang3.StringUtils
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.util.*

class FindChannelsCommand : DiscordCommand() {

    override val name: String
        get() = "findchannels"

    override val aliases: List<String>
        get() = listOf("fc")

    override val description: String
        get() = "Finds channels matching the given input"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_CHANNELS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_CHANNELS

    fun execute(message: DiscordCommandMessage, input: String) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild
                .flatMapMany { it.channels }
                .filter { x -> input == "*" || StringUtils.containsIgnoreCase(x.name, input) }
                .flatMap { channel -> Mono.zip(Mono.just(channel), channel.position) }
                .sort(Comparator.comparingInt<Tuple2<GuildChannel, Int>> { it.t2 })
                .map { it.t1 }
                .collectList()
                .switchIfEmpty(Mono.just(ArrayList()))
                .flatMap { channels ->
                    if (channels.isEmpty()) {
                        commandManager.sendResponse(message.channel, "No channels found", "No channels were found matching your input.")
                    } else {
                        val stringBuilder = StringBuilder()
                        channels.forEach { x ->
                            when (x) {
                                is Category -> stringBuilder.append("**").append(x.getMention()).append("**")
                                is VoiceChannel -> stringBuilder.append("*").append(x.getMention()).append("*")
                                else -> stringBuilder.append(x.mention)
                            }
                            stringBuilder.append(" | ").append(x.id.asString()).append('\n')
                        }

                        commandManager.sendResponse(message.channel, channels.size.toString() + " " + (if (channels.size > 1) "Channels" else "Channel") + " found matching \"" + input + "\"", stringBuilder.toString())
                    }
                }
                .subscribe()
    }

}
