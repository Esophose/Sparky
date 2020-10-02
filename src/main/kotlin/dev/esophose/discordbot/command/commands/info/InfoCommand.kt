package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Mono

class InfoCommand : DiscordCommand() {

    override val name: String
        get() = "info"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Displays information about the bot"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage) {
        Mono.zip(Sparky.discord.guilds.count(), BotUtils.watchingUserCount).subscribe { tuple ->
            val self = Sparky.self
            val guildCount = tuple.t1
            val userCount = tuple.t2
            val guildSettings = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(message.guildId)
            val prefix = guildSettings.commandPrefix

            val info = """Hi, my name is ${self.username}!
                          I'm a utility bot written in Kotlin with Discord4J `master-SNAPSHOT`.
                          Currently, I'm watching over $guildCount guilds with a total of $userCount members.
                          My prefix for this guild is `$prefix`
                          If you'd like to know more about what I can do, try out my `${prefix}help` command.""".trimIndent()

            Sparky.botInfo.owner.flatMap { owner ->
                message.channel
                        .flatMap { channel ->
                            channel.createEmbed { spec ->
                                spec.setTitle("Bot Info")
                                spec.setDescription(info)
                                spec.setThumbnail(self.avatarUrl)
                                spec.setFooter("Bot Creator: ${owner.username}#${owner.discriminator}", owner.avatarUrl)
                                spec.setColor(guildSettings.embedColor)
                                spec.setUrl("https://discordapp.com/api/oauth2/authorize?client_id=323964742538625025&permissions=2113793271&scope=bot")
                            }
                        }
            }.subscribe()
        }
    }

}
