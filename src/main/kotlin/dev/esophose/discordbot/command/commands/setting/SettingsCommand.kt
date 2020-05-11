package dev.esophose.discordbot.command.commands.setting

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Flux
import java.util.stream.Collectors

class SettingsCommand : DiscordCommand() {

    override val name: String
        get() = "settings"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "View the bot settings for this guild"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)
        val guildSettings = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(message.guildId)

        message.guild.flatMapMany { guild -> Flux.fromIterable(guildSettings.autoRoleIds).flatMap { guild.getRoleById(it) } }.collectList().flatMap { autoRoles ->
            val commandPermissions = StringBuilder("**Command permissions**\n")
            for (command in commandManager.commands) {
                val permission = command.getRequiredMemberPermission(message.guildId)
                commandPermissions.append(guildSettings.commandPrefix)
                        .append(command.name)
                        .append(" - ")
                        .append(permission.name)
                        .append("\n")
            }

            val autoRolesString = StringBuilder("Auto roles: ")
            if (autoRoles.isEmpty()) {
                autoRolesString.append("`None`")
            } else {
                autoRolesString.append("[")
                        .append(autoRoles.stream().map { it.mention }.collect(Collectors.joining(", ")))
                        .append("]")
            }

            val info = "These are the current bot settings for this guild:\n\n" +
                    "Command prefix: `" + guildSettings.commandPrefix + "`\n\n" +
                    "Embed color: `" + BotUtils.toHexString(guildSettings.embedColor) + "`\n\n" +
                    "Member join security: `" + (if (guildSettings.isMemberJoinSecurityEnabled) "enabled" else "disabled") + "`\n\n" +
                    "Message creation security: `" + (if (guildSettings.isMessageCreateSecurityEnabled) "enabled" else "disabled") + "`\n\n" +
                    autoRolesString + "\n\n" +
                    commandPermissions

            commandManager.sendResponse(message.channel, "Bot settings", info)
        }.subscribe()
    }

}
