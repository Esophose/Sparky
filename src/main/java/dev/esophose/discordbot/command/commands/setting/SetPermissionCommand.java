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

public class SetPermissionCommand extends DiscordCommand {

    public SetPermissionCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, DiscordCommand command, Permission permission) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        this.bot.getConnector().connect(connection -> {
            this.bot.getManager(GuildSettingsManager.class).updateCommandPermission(message.getGuildId(), command, permission);

            commandManager.sendResponse(message.getChannel(), "Set command permission",
                    "The permission for **." + command.getName() + "** has been changed to **" + permission.name() + "**").subscribe();
        });
    }

    @Override
    public String getName() {
        return "setpermission";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Sets the member permission required to use a command";
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
