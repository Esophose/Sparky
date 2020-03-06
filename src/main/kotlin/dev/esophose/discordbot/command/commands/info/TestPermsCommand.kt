package dev.esophose.discordbot.command.commands.info

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet

class TestPermsCommand : DiscordCommand() {

    override val name: String
        get() = "testperms"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Tests your permissions"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.author.flatMap { it.basePermissions }.subscribe { permissions ->
            val missingPermissions = PermissionSet.all().andNot(permissions)
            when {
                permissions.contains(Permission.ADMINISTRATOR) -> commandManager.sendResponse(message.channel, "You're an administrator.", "Congrats, or something.").subscribe()
                missingPermissions.size == 1 -> commandManager.sendResponse(message.channel, "You have all permissions other than administrator.", "So close to greatness.").subscribe()
                else -> {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("\n**Missing permissions: **").append('\n')
                    for (permission in missingPermissions)
                        stringBuilder.append("  - ").append(permission.name).append('\n')
                    commandManager.sendResponse(message.channel, "You are not all powerful", stringBuilder.toString()).subscribe()
                }
            }
        }
    }

}
