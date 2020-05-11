package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import dev.esophose.discordbot.webhook.WebhookUtils
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import discord4j.rest.util.Snowflake
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
                        WebhookUtils.createAndExecuteWebhook(textChannel, specDetails.t1.displayName, specDetails.t2) { spec ->
                            spec.setContent(targetMessage.content)
                            targetMessage.attachments.forEach { file -> spec.addFile(file.filename, BotUtils.getAttachment(file.url)) }
                            if (targetMessage.embeds.isNotEmpty()) {
                                val embed = targetMessage.embeds[0]
                                spec.setEmbed { embedSpec ->
                                    embed.author.ifPresent { author -> embedSpec.setAuthor(author.name, author.url, author.iconUrl) }
                                    embed.color.ifPresent { embedSpec.setColor(it) }
                                    embed.description.ifPresent { embedSpec.setDescription(it) }
                                    embed.fields.forEach { field -> embedSpec.addField(field.name, field.value, field.isInline) }
                                    embed.footer.ifPresent { footer -> embedSpec.setFooter(footer.text, footer.iconUrl) }
                                    embed.image.map { it.url }.ifPresent { embedSpec.setUrl(it) }
                                    embed.thumbnail.map { it.url }.ifPresent { embedSpec.setThumbnail(it) }
                                    embed.timestamp.ifPresent { embedSpec.setTimestamp(it) }
                                    embed.title.ifPresent { embedSpec.setTitle(it) }
                                    embed.url.ifPresent { embedSpec.setUrl(it) }
                                }
                            }
                        }
                    }.doOnSuccess {
                        targetMessage.delete("Moved message").subscribe()
                        commandManager.sendResponse(message.channel, "Message moved", "The message was moved successfully.").subscribe()
                    }.subscribe()
        }
    }

}
