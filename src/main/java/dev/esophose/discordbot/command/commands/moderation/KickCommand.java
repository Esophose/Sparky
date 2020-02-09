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

public class KickCommand extends DiscordCommand {

    public KickCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Member member, Optional<String> reason) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild()
                .flatMap(x -> reason.map(s -> x.kick(member.getId(), s)).orElseGet(() -> x.kick(member.getId())))
                .doOnError(error -> commandManager.sendResponse(message.getChannel(), "Failed to kick user", "An error occurred trying to kick that user: " + error.getMessage()).subscribe())
                .doOnSuccess(v -> commandManager.sendResponse(message.getChannel(), "User kicked", member.getMention() + " has been kicked.").subscribe())
                .subscribe();
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Kicks a member";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.KICK_MEMBERS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.KICK_MEMBERS;
    }

}
