package dev.esophose.discordbot.command.commands.setting;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.misc.GuildSettings;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class RemoveAutoRoleCommand extends DiscordCommand {

    public RemoveAutoRoleCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Role role) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        GuildSettingsManager guildSettingsManager = this.bot.getManager(GuildSettingsManager.class);

        GuildSettings guildSettings = guildSettingsManager.getGuildSettings(message.getGuildId());
        if (guildSettings.getAutoRoleIds().contains(role.getId())) {
            commandManager.sendResponse(message.getChannel(), "Auto role does not exist", "An auto role for " + role.getMention() + " does not exist.").subscribe();
            return;
        }

        guildSettingsManager.addAutoRole(message.getGuildId(), role.getId());
        commandManager.sendResponse(message.getChannel(), "Removed auto role", "The auto role for " + role.getMention() + " has been removed.").subscribe();
    }

    @Override
    public String getName() {
        return "removeautorole";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Removes a role from being automatically given to new members on join";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.ADMINISTRATOR;
    }

}
