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

public class SetMessageCreateSecurityEnabledCommand extends DiscordCommand {

    public SetMessageCreateSecurityEnabledCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Boolean enabled) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        this.bot.getConnector().connect(connection -> {
            this.bot.getManager(GuildSettingsManager.class).updateMessageCreateSecurity(message.getGuildId(), enabled);

            if (enabled) {
                commandManager.sendResponse(message.getChannel(), "Enabled message creation security",
                        "Security for rapid message creation has been enabled. If too many message are sent in channel within a certain time frame, " +
                                "the channel will be put into slowmode to prevent a raid and/or spam.").subscribe();
            } else {
                commandManager.sendResponse(message.getChannel(), "Disabled message creation security",
                        "Security for rapid message creation has been disabled. Your server may be more vulnerable to raids and channel spam with this disabled.").subscribe();
            }
        });
    }

    @Override
    public String getName() {
        return "setmessagecreatesecurityenabled";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Sets whether or not message creation security is enabled";
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
