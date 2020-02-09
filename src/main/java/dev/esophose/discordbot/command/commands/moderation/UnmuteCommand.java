package dev.esophose.discordbot.command.commands.moderation;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class UnmuteCommand extends DiscordCommand  {

    public UnmuteCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Member member) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        message.getGuild().flatMapMany(Guild::getRoles).collectList().subscribe(roles -> {
            Optional<Role> optionalMutedRole = roles.stream().filter(x -> StringUtils.equalsIgnoreCase(x.getName(), "Muted")).findFirst();
            if (!optionalMutedRole.isPresent()) {
                commandManager.sendResponse(message.getChannel(), "Not set up", "The mute command is not yet set up. Please run `.setupmute` to set up the role and channel overrides.").subscribe();
            } else {
                member.getRoles()
                        .filter(x -> x.equals(optionalMutedRole.get()))
                        .hasElements()
                        .subscribe(hasRole -> {
                    if (!hasRole) {
                        commandManager.sendResponse(message.getChannel(), "Couldn't unmute member", member.getNicknameMention() + " is not muted.").subscribe();
                    } else {
                        member.removeRole(optionalMutedRole.get().getId(), "Member was unmuted").subscribe();
                        commandManager.sendResponse(message.getChannel(), "Member unmuted", member.getNicknameMention() + " has been unmuted.").subscribe();
                    }
                });
            }
        });
    }

    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Unmutes a member";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES, Permission.MUTE_MEMBERS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MUTE_MEMBERS;
    }

}
