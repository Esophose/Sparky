package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Mono

class MoveCommand : DiscordCommand() {

    override val name: String
        get() = "move"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Moves a message from one channel to another"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_CHANNELS, Permission.MANAGE_MESSAGES, Permission.MANAGE_WEBHOOKS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_MESSAGES

    fun execute(message: DiscordCommandMessage, messageId: Snowflake, textChannel: GuildChannel) {
        val commandManager = Sparky.getManager(CommandManager::class)

        if (textChannel !is GuildMessageChannel) {
            commandManager.sendResponse(message.channel, "Invalid text channel", "The target channel must be a text channel.").subscribe()
            return
        }

        message.channel.flatMap { channel -> channel.getMessageById(messageId).doOnError { commandManager.sendResponse(message.channel, "Failed to move message", "The given message snowflake was invalid.").subscribe() } }.subscribe { targetMessage ->
            Mono.zip(targetMessage.authorAsMember, targetMessage.authorAsMember.flatMap { it.avatar })
                    .flatMap { specDetails ->
                        textChannel.createWebhook { spec ->
                            spec.setName(specDetails.t1.displayName)
                            spec.setAvatar(specDetails.t2)
                        }
                    }
                    .flatMap { webhook ->
                        webhook.execute { spec ->
                            spec.setContent(targetMessage.content)
                            targetMessage.attachments.forEach { spec.addFile(it.filename, BotUtils.getAttachment(it.url)) }
                            targetMessage.embeds.forEach { embed ->
                                spec.addEmbed { embedSpec ->
                                    embed.author.ifPresent { author -> embedSpec.setAuthor(author.name.get(), author.url.orElse(null), author.iconUrl.orElse(null)) }
                                    embed.color.ifPresent { embedSpec.setColor(it) }
                                    embed.description.ifPresent { embedSpec.setDescription(it) }
                                    embed.fields.forEach { field -> embedSpec.addField(field.name, field.value, field.isInline) }
                                    embed.footer.ifPresent { footer -> embedSpec.setFooter(footer.text, footer.iconUrl.orElse(null)) }
                                    embed.image.map { it.url }.ifPresent { embedSpec.setUrl(it) }
                                    embed.thumbnail.map { it.url }.ifPresent { embedSpec.setThumbnail(it) }
                                    embed.timestamp.ifPresent { embedSpec.setTimestamp(it) }
                                    embed.title.ifPresent { embedSpec.setTitle(it) }
                                    embed.url.ifPresent { embedSpec.setUrl(it) }
                                }
                            }
                        }.then(webhook.delete())
                    }
                    .doOnSuccess {
                        targetMessage.delete("Moved message").subscribe()
                        commandManager.sendResponse(message.channel, "Message moved", "The message was moved successfully.").subscribe()
                    }.subscribe()
        }
    }

}
