package dev.esophose.discordbot.command.commands.setting;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class SetEmbedColorCommand extends DiscordCommand {

    public SetEmbedColorCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Color color) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        this.bot.getManager(GuildSettingsManager.class).updateEmbedColor(message.getGuildId(), color);

        commandManager.sendResponse(message.getChannel(), "Set bot embed color", "The bot message embed color for this guild has been changed to `" +
                BotUtils.toHexString(color) + "`").subscribe();
    }

    @Override
    public String getName() {
        return "setembedcolor";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Sets the bot message embed color for this guild";
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
