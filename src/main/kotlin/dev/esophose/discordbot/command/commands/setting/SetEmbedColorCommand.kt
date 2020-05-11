package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import java.awt.Color

class SetEmbedColorCommand : DiscordCommand() {

    override val name: String
        get() = "setembedcolor"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Sets the bot message embed color for this guild"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, color: Color) {
        Sparky.getManager(GuildSettingsManager::class).updateEmbedColor(message.guildId, color)
        Sparky.getManager(CommandManager::class).sendResponse(message.channel, "Set bot embed color", "The bot message embed color for this guild has been changed to `" +
                BotUtils.toHexString(color) + "`").subscribe()
    }

}
