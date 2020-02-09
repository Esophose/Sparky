package dev.esophose.discordbot.command.commands.moderation;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BanCommand extends DiscordCommand {

    public BanCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Member member, Optional<String> reason) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild()
                .flatMap(x -> x.ban(member.getId(), spec -> reason.ifPresent(spec::setReason)))
                .doOnError(error -> commandManager.sendResponse(message.getChannel(), "Failed to ban user", "An error occurred trying to ban that user: " + error.getMessage()).subscribe())
                .doOnSuccess(v -> commandManager.sendResponse(message.getChannel(), "User banned", member.getMention() + " has been banned.").subscribe())
                .subscribe();
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Bans a member";
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
