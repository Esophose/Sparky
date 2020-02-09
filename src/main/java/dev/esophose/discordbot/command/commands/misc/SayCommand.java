package dev.esophose.discordbot.command.commands.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class SayCommand extends DiscordCommand {

    public SayCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, String text) {
        message.delete().subscribe();
        message.getChannel()
                .cast(GuildMessageChannel.class)
                .flatMap(x -> x.createMessage(text))
                .subscribe();
    }

    @Override
    public String getName() {
        return "say";
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
