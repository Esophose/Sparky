package dev.esophose.discordbot.command.commands.info

import com.google.common.collect.Lists
import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Member
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Flux
import reactor.util.function.Tuple2
import java.util.Comparator
import java.util.HashMap

class SecurityAuditCommand : DiscordCommand() {

    override val name: String
        get() = "securityaudit"

    override val aliases: List<String>
        get() = Lists.newArrayList("sa", "audit")

    override val description: String
        get() = "Perform a security audit of your guild"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.MANAGE_ROLES

    fun execute(message: DiscordCommandMessage) {
        val commandManager = Sparky.getManager(CommandManager::class)

        message.guild
                .flatMapMany { it.members }
                .flatMap { it.basePermissions }
                .collectList()
                .flatMap { permissions -> commandManager.sendResponse(message.channel, "User permission amounts", this.getPermissionCountsString(permissions)) }
                .subscribe()

        message.guild
                .flatMapMany { it.members }
                .collectList()
                .flatMap { members ->
                    Flux.zip(Flux.fromIterable<Member>(members), Flux.fromIterable<Member>(members).flatMap { it.basePermissions })
                            .collectList()
                            .flatMap { tuple -> commandManager.sendResponse(message.channel, "Administrative users", this.getAdministrativeCountsString(tuple)) }
                }.subscribe()
    }

    private fun getPermissionCountsString(permissions: List<PermissionSet>): String {
        val permissionCounts = HashMap<Permission, Int>()
        for (permissionSet in permissions) {
            for (permission in permissionSet) {
                if (permissionCounts.containsKey(permission)) {
                    permissionCounts[permission] = (permissionCounts[permission]!! + 1)
                } else {
                    permissionCounts[permission] = 1
                }
            }
        }

        val stringBuilder = StringBuilder()
        permissionCounts.entries
                .stream()
                .sorted(Comparator.comparingLong { entry -> entry.key.value })
                .forEach { entry -> stringBuilder.append(entry.key.name).append(": ").append(entry.value).append('\n') }

        return stringBuilder.toString()
    }

    private fun getAdministrativeCountsString(permissions: List<Tuple2<Member, PermissionSet>>): String {
        val stringBuilder = StringBuilder("The following members have ADMINISTATOR access:\n")
        permissions.stream()
                .filter { x -> x.t2.contains(Permission.ADMINISTRATOR) }
                .filter { x -> !x.t1.isBot }
                .forEach { x -> stringBuilder.append(x.t1.nicknameMention).append('\n') }

        val botAdminsCount = permissions.stream()
                .filter { x -> x.t2.contains(Permission.ADMINISTRATOR) }
                .filter { x -> x.t1.isBot }
                .count()

        if (botAdminsCount > 0) {
            stringBuilder.append("\n\n\u26a0 **WARNING! YOU HAVE (")
                    .append(botAdminsCount)
                    .append(") ADMINISTRATIVE BOTS!**")
                    .append("\nBots with administrative permissions are usually a *very bad* idea.")
                    .append("\nYou should restrict these bots to only have the minimum permissions they require:\n")
            permissions.stream()
                    .filter { x -> x.t2.contains(Permission.ADMINISTRATOR) }
                    .filter { x -> x.t1.isBot }
                    .forEach { x -> stringBuilder.append(x.t1.nicknameMention).append('\n') }
        }

        return stringBuilder.toString()
    }

}
