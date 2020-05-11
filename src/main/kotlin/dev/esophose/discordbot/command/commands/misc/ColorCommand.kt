package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import java.awt.Color

class ColorCommand : DiscordCommand() {

    override val name: String
        get() = "color"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Changes your name color"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_ROLES

    fun execute(message: DiscordCommandMessage, color: Color) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild.subscribe { guild ->
            message.author.subscribe { member ->
                member.roles.collectList().subscribe { roles ->
                    roles.stream().filter { x -> x.name.startsWith("Color-#") }.forEach { role -> role.delete().subscribe() }

                    val colorString = BotUtils.toHexString(color)

                    guild.createRole { spec ->
                        spec.setName("Color-$colorString")
                        spec.setMentionable(false)
                        spec.setHoist(false)
                        spec.setColor(color)
                        spec.setPermissions(PermissionSet.none())
                    }.map { it.id }.flatMap { member.addRole(it) }.subscribe()

                    commandManager.sendResponse(message.channel, "Color added", "Your role color has been set to `$colorString`!").subscribe()
                }
            }
        }
    }

}
