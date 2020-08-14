package dev.esophose.discordbot.manager

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.misc.GuildSettings
import dev.esophose.discordbot.utils.BotUtils
import discord4j.common.util.Snowflake
import discord4j.rest.util.Color
import discord4j.rest.util.Permission
import java.sql.Connection
import java.util.ArrayList
import java.util.HashMap

class GuildSettingsManager : Manager() {

    private val guildSettings: MutableMap<Snowflake, GuildSettings>
    private val commandManager: CommandManager

    init {
        this.guildSettings = HashMap()
        this.commandManager = Sparky.getManager(CommandManager::class)
    }

    override fun enable() {

    }

    fun loadGuildSettings(guildId: Snowflake) {
        val optionalGuild = Sparky.discord.getGuildById(guildId).blockOptional()
        if (this.guildSettings.containsKey(guildId) || optionalGuild.isEmpty)
            return

        val guild = optionalGuild.get()

        Sparky.connector.connect { connection ->
            val settings = GuildSettings.default
            this.guildSettings[guildId] = settings

            val commandPermissionOverrides = "SELECT command_name, required_permission FROM command_permission_override WHERE guild_id = ?"
            connection.prepareStatement(commandPermissionOverrides).use { statement ->
                statement.setLong(1, guildId.asLong())

                val result = statement.executeQuery()
                while (result.next()) {
                    val command = this.commandManager.getCommand(result.getString("command_name"))!!
                    val permission = Permission.valueOf(result.getString("required_permission"))
                    settings.commandPermissions[command] = permission
                }
            }

            val securityOptions = "SELECT member_join, message_create FROM security_options WHERE guild_id = ?"
            connection.prepareStatement(securityOptions).use { statement ->
                statement.setLong(1, guildId.asLong())

                val result = statement.executeQuery()
                if (result.next()) {
                    settings.isMemberJoinSecurityEnabled = result.getBoolean("member_join")
                    settings.isMessageCreateSecurityEnabled = result.getBoolean("message_create")
                }
            }

            val commandPrefix = "SELECT prefix FROM command_prefix WHERE guild_id = ?"
            connection.prepareStatement(commandPrefix).use { statement ->
                statement.setLong(1, guildId.asLong())

                val result = statement.executeQuery()
                if (result.next())
                    settings.commandPrefix = result.getString(1)
            }

            val embedColor = "SELECT color FROM embed_color WHERE guild_id = ?"
            connection.prepareStatement(embedColor).use { statement ->
                statement.setLong(1, guildId.asLong())

                val result = statement.executeQuery()
                if (result.next())
                    settings.embedColor = Color.of(java.awt.Color.decode(result.getString(1)).rgb)
            }

            val autoRoles = "SELECT role_id FROM auto_role WHERE guild_id = ?"
            connection.prepareStatement(autoRoles).use { statement ->
                statement.setLong(1, guildId.asLong())

                val autoRoleIds = settings.autoRoleIds
                val result = statement.executeQuery()
                while (result.next())
                    autoRoleIds.add(Snowflake.of(result.getLong(1)))

                // Validate roles still exist, this needs to be a blocking operation
                if (autoRoleIds.isNotEmpty()) {
                    val removed = ArrayList<Snowflake>()
                    val autoRoleIdsIterator = autoRoleIds.iterator()
                    while (autoRoleIdsIterator.hasNext()) {
                        val roleId = autoRoleIdsIterator.next()
                        if (guild.getRoleById(roleId).blockOptional().isEmpty) {
                            autoRoleIdsIterator.remove()
                            removed.add(roleId)
                        }
                    }

                    // Some roles were removed, delete them
                    if (removed.isNotEmpty())
                        removed.forEach { x -> this.removeAutoRole(guildId, x) }
                }
            }
        }
    }

    fun unloadGuildSettings(guildId: Snowflake) {
        this.guildSettings.remove(guildId)
    }

    fun getGuildSettings(guildId: Snowflake): GuildSettings {
        if (!this.guildSettings.containsKey(guildId))
            this.loadGuildSettings(guildId)

        return this.guildSettings[guildId]!!
    }

    fun updateCommandPermission(guildId: Snowflake, command: DiscordCommand, permission: Permission) {
        this.getGuildSettings(guildId).commandPermissions[command] = permission

        Sparky.connector.connect { connection: Connection ->
            val update = "REPLACE INTO command_permission_override (guild_id, command_name, required_permission) VALUES (?, ?, ?)"
            connection.prepareStatement(update).use { statement ->
                statement.setLong(1, guildId.asLong())
                statement.setString(2, command.name)
                statement.setString(3, permission.name)
                statement.executeUpdate()
            }
        }
    }

    fun updateMemberJoinSecurity(guildId: Snowflake, enabled: Boolean) {
        val settings = this.getGuildSettings(guildId)
        settings.isMemberJoinSecurityEnabled = enabled

        Sparky.connector.connect { connection ->
            val update = "REPLACE INTO security_options (guild_id, member_join, message_create) VALUES (?, ?, ?)"
            connection.prepareStatement(update).use { statement ->
                statement.setLong(1, guildId.asLong())
                statement.setBoolean(2, enabled)
                statement.setBoolean(3, settings.isMessageCreateSecurityEnabled)
                statement.executeUpdate()
            }
        }
    }

    fun updateMessageCreateSecurity(guildId: Snowflake, enabled: Boolean) {
        val settings = this.getGuildSettings(guildId)
        settings.isMessageCreateSecurityEnabled = enabled

        Sparky.connector.connect { connection ->
            val update = "REPLACE INTO security_options (guild_id, member_join, message_create) VALUES (?, ?, ?)"
            connection.prepareStatement(update).use { statement ->
                statement.setLong(1, guildId.asLong())
                statement.setBoolean(2, settings.isMemberJoinSecurityEnabled)
                statement.setBoolean(3, enabled)
                statement.executeUpdate()
            }
        }
    }

    fun updateCommandPrefix(guildId: Snowflake, commandPrefix: String) {
        this.getGuildSettings(guildId).commandPrefix = commandPrefix

        Sparky.connector.connect { connection ->
            val update = "REPLACE INTO command_prefix (guild_id, prefix) VALUES (?, ?)"
            connection.prepareStatement(update).use { statement ->
                statement.setLong(1, guildId.asLong())
                statement.setString(2, commandPrefix)
                statement.executeUpdate()
            }
        }
    }

    fun updateEmbedColor(guildId: Snowflake, color: Color) {
        this.getGuildSettings(guildId).embedColor = color

        Sparky.connector.connect { connection ->
            val update = "REPLACE INTO embed_color (guild_id, color) VALUES (?, ?)"
            connection.prepareStatement(update).use { statement ->
                statement.setLong(1, guildId.asLong())
                statement.setString(2, BotUtils.toHexString(color))
                statement.executeUpdate()
            }
        }
    }

    fun addAutoRole(guildId: Snowflake, roleId: Snowflake) {
        this.getGuildSettings(guildId).autoRoleIds.add(roleId)

        Sparky.connector.connect { connection ->
            val delete = "INSERT INTO auto_role (guild_id, role_id) VALUES (?, ?)"
            connection.prepareStatement(delete).use { removeStatement ->
                removeStatement.setLong(1, guildId.asLong())
                removeStatement.setLong(2, roleId.asLong())
                removeStatement.executeUpdate()
            }
        }
    }

    fun removeAutoRole(guildId: Snowflake, roleId: Snowflake) {
        this.getGuildSettings(guildId).autoRoleIds.remove(roleId)

        Sparky.connector.connect { connection ->
            val delete = "DELETE FROM auto_role WHERE guild_id = ? AND role_id = ?"
            connection.prepareStatement(delete).use { removeStatement ->
                removeStatement.setLong(1, guildId.asLong())
                removeStatement.setLong(2, roleId.asLong())
                removeStatement.executeUpdate()
            }
        }
    }

}
