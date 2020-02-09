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

public class SetPrefixCommand extends DiscordCommand {

    public SetPrefixCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, String prefix) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        if (prefix.isBlank()) {
            commandManager.sendResponse(message.getChannel(), "Invalid prefix", "The prefix must contain at least one non-whitespace character").subscribe();
            return;
        }

        prefix = prefix.trim();

        this.bot.getManager(GuildSettingsManager.class).updateCommandPrefix(message.getGuildId(), prefix);

        commandManager.sendResponse(message.getChannel(), "Set bot prefix", "The bot command prefix for this guild has been changed to `" + prefix + "`").subscribe();
    }

    @Override
    public String getName() {
        return "setprefix";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Sets the bot prefix for this guild";
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
