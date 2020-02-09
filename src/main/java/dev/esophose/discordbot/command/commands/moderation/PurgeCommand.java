package dev.esophose.discordbot.command.commands.moderation;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class PurgeCommand extends DiscordCommand {

    public PurgeCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Integer amount) {
        message.delete().subscribe();
        message.getChannel()
                .cast(GuildMessageChannel.class)
                .subscribe(channel -> channel.bulkDelete(
                        channel.getMessagesBefore(channel.getLastMessageId().orElse(message.getMessageId()))
                        .take(amount)
                        .map(Message::getId))
                        .subscribe());
    }

    @Override
    public String getName() {
        return "purge";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Purges a section of chat";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.MANAGE_MESSAGES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MANAGE_MESSAGES;
    }

}
