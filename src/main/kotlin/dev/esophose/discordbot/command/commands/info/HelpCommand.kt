package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.manager.PaginatedEmbedManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet

class HelpCommand : DiscordCommand() {

    override val name: String
        get() = "help"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Displays commands you can access"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_MESSAGES, Permission.ADD_REACTIONS)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage) {
        message.author.flatMap { it.basePermissions }.subscribe { permissions ->
            val paginatedEmbedManager = Sparky.getManager(PaginatedEmbedManager::class)
            val commandManager = Sparky.getManager(CommandManager::class)
            val guildSettings = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(message.guildId)

            if (!commandManager.canAccessCommands(message.guildId, message.authorId, permissions)) {
                commandManager.sendResponse(message.channel, "No access", "You don't have access to use any commands!").subscribe()
                return@subscribe
            }

            paginatedEmbedManager.createPaginatedEmbed(message.authorId, message.channelId) { builder ->
                val commandModules = commandManager.commandModules
                for (module in commandModules) {
                    val commands = mutableListOf(*module.loadedCommands.toTypedArray())
                    if (module.name != "owner") {
                        commands.removeIf { !permissions.contains(it.getRequiredMemberPermission(message.guildId)) }
                    } else if (Sparky.botInfo.ownerId != message.authorId) {
                        continue
                    }

                    builder.addPage { embed ->
                        val emoji = module.icon.asUnicodeEmoji()
                        if (emoji.isPresent) {
                            embed.setTitle(emoji.get().raw + "  " + module.name + " Commands")
                        } else {
                            embed.setTitle(module.name + " Commands")
                        }

                        val description = StringBuilder()
                        for (command in commands)
                            description.append(commandManager.getCommandUsage(command, true, guildSettings.commandPrefix)).append(" - ").append(command.description).append("\n\n")
                        embed.setDescription(description.toString())
                        embed.setColor(guildSettings.embedColor)
                        embed.setFooter("Page %currentPage%/%maxPage%", null)
                    }
                }
            }.subscribe()
        }
    }

}
