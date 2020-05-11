package dev.esophose.discordbot.command.commands.moderation

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet

class MassRoleCommand : DiscordCommand() {

    override val name: String
        get() = "massrole"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Gives a role to all non-bot guild members"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_MESSAGES

    fun execute(message: DiscordCommandMessage, role: Role) {
        val commandManager = Sparky.getManager(CommandManager::class)

        if (role.isEveryone) {
            commandManager.sendResponse(message.channel, "Don't be silly", "You can't add a role for `@everyone`... They already have that by default.").subscribe()
            return
        }

        message.guild.flatMap { guild -> guild.getMemberById(Sparky.self.id) }
                .flatMap { it.highestRole }
                .subscribe { highestRole ->
                    highestRole.position
                            .zipWith(role.position)
                            .subscribe { tuple ->
                                if (tuple.t1 <= tuple.t2) {
                                    commandManager.sendResponse(message.channel, "Role hierarchy issue", "My highest role is ${highestRole.mention} but ${role.mention} has a lower role position. Move the desired mass role below my highest role then try this command again.").subscribe()
                                } else {
                                    message.guild.flatMapMany { it.members }
                                            .filter { x -> !x.isBot }
                                            .filter { x -> !x.roleIds.contains(role.id) }
                                            .count()
                                            .subscribe { amount ->
                                                if (amount == 0L) {
                                                    commandManager.sendResponse(message.channel, "That was quick", "All non-bot members in this guild already have ${role.mention} added to their roles.").subscribe()
                                                } else {
                                                    commandManager.sendResponse(message.channel, "Adding roles", "Started granting ${role.mention} to $amount members. This may take a while for large guilds.").subscribe()
                                                    message.guild.flatMapMany { it.members }
                                                            .filter { x -> !x.isBot }
                                                            .filter { x -> !x.roleIds.contains(role.id) }
                                                            .flatMap { x -> x.addRole(role.id) }
                                                            .subscribe()
                                                }
                                            }
                                }
                            }
                }
    }

}
