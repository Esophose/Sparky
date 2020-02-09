package dev.esophose.discordbot.command.commands.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbedCommand extends DiscordCommand {

    public EmbedCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, String title, String text) {
        message.delete().subscribe();

        if (title.equals("null"))
            title = null;

        text = text.replaceAll(Pattern.quote("\\n"), Matcher.quoteReplacement("\n"));

        this.bot.getManager(CommandManager.class).sendResponse(message.getChannel(), title, text).subscribe();
    }

    @Override
    public String getName() {
        return "embed";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Makes the bot say a message";
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
