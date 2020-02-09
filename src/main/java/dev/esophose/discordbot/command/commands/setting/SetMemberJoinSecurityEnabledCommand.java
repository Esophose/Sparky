package dev.esophose.discordbot.command.commands.setting;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class SetMemberJoinSecurityEnabledCommand extends DiscordCommand {

    public SetMemberJoinSecurityEnabledCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Boolean enabled) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        this.bot.getConnector().connect(connection -> {
            this.bot.getManager(GuildSettingsManager.class).updateMemberJoinSecurity(message.getGuildId(), enabled);

            if (enabled) {
                commandManager.sendResponse(message.getChannel(), "Enabled member join security",
                        "Security for rapid member joining has been enabled. If too many members join within a certain time frame, " +
                        "joining will be temporarily disabled to prevent a raid.").subscribe();
            } else {
                commandManager.sendResponse(message.getChannel(), "Disabled member join security",
                        "Security for rapid member joining has been disabled. Your server may be more vulnerable to raids with this disabled.").subscribe();
            }
        });
    }

    @Override
    public String getName() {
        return "setmemberjoinsecurityenabled";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Sets whether or not rapid member join security is enabled";
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
