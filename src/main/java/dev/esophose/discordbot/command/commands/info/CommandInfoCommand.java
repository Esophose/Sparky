package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInfoCommand extends DiscordCommand {

    public CommandInfoCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, DiscordCommand command) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        String commandPrefix = this.bot.getManager(GuildSettingsManager.class).getGuildSettings(message.getGuildId()).getCommandPrefix();

        String info =
                "**Command Name:** " + command.getName() + '\n' +
                        "**Aliases:** " + "[" + String.join(", ", command.getAliases()) + "]" + '\n' +
                        "**Description:** " + command.getDescription() + '\n' +
                        "**Parameters:** " + commandManager.getCommandUsage(command, false, commandPrefix) + '\n' +
                        "**Required Member Permission:** " + command.getRequiredMemberPermission(message.getGuildId()).name() + '\n' +
                        "**Required Bot Permissions:** " + "[" + command.getRequiredBotPermissions().stream().map(Enum::name).collect(Collectors.joining(", ")) + "]";
        commandManager.sendResponse(message.getChannel(), "Info for ." + command.getName(), info).subscribe();
    }

    @Override
    public String getName() {
        return "cinfo";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("commandinfo");
    }

    @Override
    public String getDescription() {
        return "Displays info for a command";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.SEND_MESSAGES;
    }

}
