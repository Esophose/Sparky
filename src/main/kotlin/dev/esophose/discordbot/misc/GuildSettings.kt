package dev.esophose.discordbot.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.Snowflake
import java.awt.Color
import java.time.Instant
import java.util.ArrayList
import java.util.HashMap

class GuildSettings(val guildId: Snowflake,
                    val commandPermissions: MutableMap<DiscordCommand, Permission>,
                    var commandPrefix: String,
                    var isMemberJoinSecurityEnabled: Boolean,
                    var isMessageCreateSecurityEnabled: Boolean,
                    var embedColor: Color,
                    val autoRoleIds: MutableList<Snowflake>) {

    companion object {
        val default: GuildSettings
            get() {
                val commandPermissions = HashMap<DiscordCommand, Permission>()
                for (command in Sparky.getManager(CommandManager::class).commands)
                    commandPermissions[command] = command.defaultRequiredMemberPermission

                return GuildSettings(
                        Snowflake.of(Instant.now()),
                        commandPermissions,
                        CommandManager.DEFAULT_PREFIX,
                        isMemberJoinSecurityEnabled = true,
                        isMessageCreateSecurityEnabled = true,
                        embedColor = CommandManager.DEFAULT_EMBED_COLOR,
                        autoRoleIds = ArrayList()
                )
            }
    }

}
