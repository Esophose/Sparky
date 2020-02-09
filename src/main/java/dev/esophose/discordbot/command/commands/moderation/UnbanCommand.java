package dev.esophose.discordbot.command.commands.moderation;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UnbanCommand extends DiscordCommand {

    public UnbanCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Snowflake memberId, Optional<String> reason) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild()
                .flatMapMany(Guild::getBans)
                .filter(ban -> ban.getUser().getId().equals(memberId))
                .hasElements()
                .subscribe(isBanned -> {
                    if (!isBanned) {
                        commandManager.sendResponse(message.getChannel(), "Failed to unban user", memberId.asString() + " is not banned.").subscribe();
                    } else {
                        message.getGuild()
                                .flatMap(x -> reason.map(s -> x.unban(memberId, s)).orElseGet(() -> x.unban(memberId)))
                                .doOnSuccess(v -> commandManager.sendResponse(message.getChannel(), "User unbanned", memberId.asString() + " has been unbanned.").subscribe())
                                .subscribe();
                    }
                });
    }

    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Lifts a member ban";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.BAN_MEMBERS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.BAN_MEMBERS;
    }

}
