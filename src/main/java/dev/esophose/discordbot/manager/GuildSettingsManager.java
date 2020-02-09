package dev.esophose.discordbot.manager;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.misc.GuildSettings;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GuildSettingsManager extends Manager {

    private Map<Snowflake, GuildSettings> guildSettings;
    private CommandManager commandManager;

    public GuildSettingsManager(Sparky bot) {
        super(bot);

        this.guildSettings = new HashMap<>();
        this.commandManager = this.bot.getManager(CommandManager.class);
    }

    @Override
    public void enable() {

    }

    public void loadGuildSettings(Snowflake guildId) {
        Optional<Guild> optionalGuild = this.bot.getDiscord().getGuildById(guildId).blockOptional();
        if (this.guildSettings.containsKey(guildId) || optionalGuild.isEmpty())
            return;

        Guild guild = optionalGuild.get();
        this.bot.getConnector().connect(connection -> {
            GuildSettings settings = GuildSettings.getDefault();
            this.guildSettings.put(guildId, settings);

            String commandPermissionOverrides = "SELECT command_name, required_permission FROM command_permission_override WHERE guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(commandPermissionOverrides)) {
                statement.setLong(1, guildId.asLong());

                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    DiscordCommand command = this.commandManager.getCommand(result.getString("command_name"));
                    Permission permission = Permission.valueOf(result.getString("required_permission"));
                    settings.getCommandPermissions().put(command, permission);
                }
            }

            String securityOptions = "SELECT member_join, message_create FROM security_options WHERE guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(securityOptions)) {
                statement.setLong(1, guildId.asLong());

                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    settings.setMemberJoinSecurityEnabled(result.getBoolean("member_join"));
                    settings.setMessageCreateSecurityEnabled(result.getBoolean("message_create"));
                }
            }

            String commandPrefix = "SELECT prefix FROM command_prefix WHERE guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(commandPrefix)) {
                statement.setLong(1, guildId.asLong());

                ResultSet result = statement.executeQuery();
                if (result.next())
                    settings.setCommandPrefix(result.getString(1));
            }

            String embedColor = "SELECT color FROM embed_color WHERE guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(embedColor)) {
                statement.setLong(1, guildId.asLong());

                ResultSet result = statement.executeQuery();
                if (result.next())
                    settings.setEmbedColor(Color.decode(result.getString(1)));
            }

            String autoRoles = "SELECT role_id FROM auto_role WHERE guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(autoRoles)) {
                statement.setLong(1, guildId.asLong());

                List<Snowflake> autoRoleIds = settings.getAutoRoleIds();
                ResultSet result = statement.executeQuery();
                while (result.next())
                    autoRoleIds.add(Snowflake.of(result.getLong(1)));

                // Validate roles still exist, this needs to be a blocking operation
                if (!autoRoleIds.isEmpty()) {
                    List<Snowflake> removed = new ArrayList<>();
                    Iterator<Snowflake> autoRoleIdsIterator = autoRoleIds.iterator();
                    while (autoRoleIdsIterator.hasNext()) {
                        Snowflake roleId = autoRoleIdsIterator.next();
                        if (guild.getRoleById(roleId).blockOptional().isEmpty()) {
                            autoRoleIdsIterator.remove();
                            removed.add(roleId);
                        }
                    }

                    // Some roles were removed, delete them
                    if (!removed.isEmpty())
                        removed.forEach(x -> this.removeAutoRole(guildId, x));
                }
            }
        });
    }

    public void unloadGuildSettings(Snowflake guildId) {
        this.guildSettings.remove(guildId);
    }

    public GuildSettings getGuildSettings(Snowflake guildId) {
        if (!this.guildSettings.containsKey(guildId))
            this.loadGuildSettings(guildId);

        return this.guildSettings.get(guildId);
    }

    public void updateCommandPermission(Snowflake guildId, DiscordCommand command, Permission permission) {
        this.getGuildSettings(guildId).getCommandPermissions().put(command, permission);

        this.bot.getConnector().connect(connection -> {
            String update = "REPLACE INTO command_permission_override (guild_id, command_name, required_permission) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setLong(1, guildId.asLong());
                statement.setString(2, command.getName());
                statement.setString(3, permission.name());
                statement.executeUpdate();
            }
        });
    }

    public void updateMemberJoinSecurity(Snowflake guildId, boolean enabled) {
        GuildSettings settings = this.getGuildSettings(guildId);
        settings.setMemberJoinSecurityEnabled(enabled);

        this.bot.getConnector().connect(connection -> {
            String update = "REPLACE INTO security_options (guild_id, member_join, message_create) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setLong(1, guildId.asLong());
                statement.setBoolean(2, enabled);
                statement.setBoolean(3, settings.isMessageCreateSecurityEnabled());
                statement.executeUpdate();
            }
        });
    }

    public void updateMessageCreateSecurity(Snowflake guildId, boolean enabled) {
        GuildSettings settings = this.getGuildSettings(guildId);
        settings.setMessageCreateSecurityEnabled(enabled);

        this.bot.getConnector().connect(connection -> {
            String update = "REPLACE INTO security_options (guild_id, member_join, message_create) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setLong(1, guildId.asLong());
                statement.setBoolean(2, settings.isMemberJoinSecurityEnabled());
                statement.setBoolean(3, enabled);
                statement.executeUpdate();
            }
        });
    }

    public void updateCommandPrefix(Snowflake guildId, String commandPrefix) {
        this.getGuildSettings(guildId).setCommandPrefix(commandPrefix);

        this.bot.getConnector().connect(connection -> {
            String update = "REPLACE INTO command_prefix (guild_id, prefix) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setLong(1, guildId.asLong());
                statement.setString(2, commandPrefix);
                statement.executeUpdate();
            }
        });
    }

    public void updateEmbedColor(Snowflake guildId, Color color) {
        this.getGuildSettings(guildId).setEmbedColor(color);

        this.bot.getConnector().connect(connection -> {
            String update = "REPLACE INTO embed_color (guild_id, color) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setLong(1, guildId.asLong());
                statement.setString(2, BotUtils.toHexString(color));
                statement.executeUpdate();
            }
        });
    }

    public void addAutoRole(Snowflake guildId, Snowflake roleId) {
        this.getGuildSettings(guildId).getAutoRoleIds().add(roleId);

        this.bot.getConnector().connect(connection -> {
            String delete = "INSERT INTO auto_role (guild_id, role_id) VALUES (?, ?)";
            try (PreparedStatement removeStatement = connection.prepareStatement(delete)) {
                removeStatement.setLong(1, guildId.asLong());
                removeStatement.setLong(2, roleId.asLong());
                removeStatement.executeUpdate();
            }
        });
    }

    public void removeAutoRole(Snowflake guildId, Snowflake roleId) {
        this.getGuildSettings(guildId).getAutoRoleIds().remove(roleId);

        this.bot.getConnector().connect(connection -> {
            String delete = "DELETE FROM auto_role WHERE guild_id = ? AND role_id = ?";
            try (PreparedStatement removeStatement = connection.prepareStatement(delete)) {
                removeStatement.setLong(1, guildId.asLong());
                removeStatement.setLong(2, roleId.asLong());
                removeStatement.executeUpdate();
            }
        });
    }

}
