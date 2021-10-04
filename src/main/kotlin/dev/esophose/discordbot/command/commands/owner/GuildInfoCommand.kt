package dev.esophose.discordbot.command.commands.owner

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.core.`object`.entity.Guild
import discord4j.rest.util.Image
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.text.WordUtils
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Optional

class GuildInfoCommand : DiscordCommand(true) {

    override val name: String
        get() = "guildinfo"

    override val aliases: List<String>
        get() = listOf("ginfo")

    override val description: String
        get() = "Displays info for a guild"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, guild: Optional<Guild>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        guild.map { Mono.just(it) }
                .orElseGet { message.guild }
                .flatMap { target ->
                    target.owner.flatMap { owner ->
                        target.channels.count().flatMap { channelCount ->
                            var iconUrl = target.getIconUrl(Image.Format.GIF).orElse(null)
                            if (iconUrl != null && target.features.contains("ANIMATED_ICON")) {
                                val request = Request.Builder()
                                    .url(iconUrl)
                                    .method("GET", null)
                                    .build()
                                val responseCode = OkHttpClient().newBuilder().build().newCall(request).execute().code
                                if (responseCode != 200)
                                    iconUrl = target.getIconUrl(Image.Format.PNG).orElse(null)
                            } else {
                                iconUrl = target.getIconUrl(Image.Format.PNG).orElse(null)
                            }

                            target.requestMembers().count().flatMap { memberCount ->
                                val info = "**Snowflake:** " + target.id.asString() + '\n' +
                                        "**Owner:** " + owner.mention + '\n' +
                                        "**Guild Creation Time:** " + BotUtils.formatDateTime(LocalDateTime.ofInstant(target.joinTime, ZoneOffset.UTC)) + '\n' +
                                        "**Member Count**: " + memberCount + '\n' +
                                        "**Role Count:** " + target.roleIds.size + '\n' +
                                        "**Emote Count:** " + target.emojiIds.size + '\n' +
                                        "**Channel Count:** " + channelCount + '\n' +
                                        "**Nitro Boosts:** " + target.premiumSubscriptionCount.orElse(0) + '\n' +
                                        "**Boost Tier:** " + WordUtils.capitalizeFully(target.premiumTier.name.replace('_', ' ')) + '\n' +
                                        "**Verification Level:** " + WordUtils.capitalizeFully(target.verificationLevel.name.replace('_', ' ')) + '\n' +
                                        "**Content Filter Level:** " + WordUtils.capitalizeFully(target.contentFilterLevel.name.replace('_', ' ')) + '\n' +
                                        "**Notification Level:** " + WordUtils.capitalizeFully(target.notificationLevel.name.replace('_', ' ')) + '\n' +
                                        "**MFA Level:** " + WordUtils.capitalizeFully(target.mfaLevel.name.replace('_', ' '))
                                commandManager.sendResponse(message.channel, "Info for " + target.name, info, iconUrl)
                            }
                        }
                    }
                }.subscribe()
    }

}
