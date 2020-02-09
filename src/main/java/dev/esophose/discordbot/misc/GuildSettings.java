package dev.esophose.discordbot.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildSettings {

    private Snowflake guildId;
    private Map<DiscordCommand, Permission> commandPermissions;
    private String commandPrefix;
    private boolean memberJoinSecurityEnabled;
    private boolean messageCreateSecurityEnabled;
    private Color embedColor;
    private List<Snowflake> autoRoles;

    public GuildSettings(Snowflake guildId, Map<DiscordCommand, Permission> commandPermissions, String commandPrefixOverride, boolean memberJoinSecurityEnabled, boolean messageCreateSecurityEnabled, Color embedColor, List<Snowflake> autoRoles) {
        this.guildId = guildId;
        this.commandPermissions = commandPermissions;
        this.commandPrefix = commandPrefixOverride;
        this.memberJoinSecurityEnabled = memberJoinSecurityEnabled;
        this.messageCreateSecurityEnabled = messageCreateSecurityEnabled;
        this.embedColor = embedColor;
        this.autoRoles = autoRoles;
    }

    public static GuildSettings getDefault() {
        Map<DiscordCommand, Permission> commandPermissions = new HashMap<>();
        for (DiscordCommand command : Sparky.getInstance().getManager(CommandManager.class).getCommands())
            commandPermissions.put(command, command.getDefaultRequiredMemberPermission());

        return new GuildSettings(null, commandPermissions, CommandManager.DEFAULT_PREFIX, true, true, CommandManager.DEFAULT_EMBED_COLOR, new ArrayList<>());
    }

    public Snowflake getGuildId() {
        return this.guildId;
    }

    public Map<DiscordCommand, Permission> getCommandPermissions() {
        return this.commandPermissions;
    }

    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public boolean isMemberJoinSecurityEnabled() {
        return this.memberJoinSecurityEnabled;
    }

    public void setMemberJoinSecurityEnabled(boolean enabled) {
        this.memberJoinSecurityEnabled = enabled;
    }

    public boolean isMessageCreateSecurityEnabled() {
        return this.messageCreateSecurityEnabled;
    }

    public void setMessageCreateSecurityEnabled(boolean enabled) {
        this.messageCreateSecurityEnabled = enabled;
    }

    public Color getEmbedColor() {
        return this.embedColor;
    }

    public void setEmbedColor(Color color) {
        this.embedColor = color;
    }

    public List<Snowflake> getAutoRoleIds() {
        return this.autoRoles;
    }

}
