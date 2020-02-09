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

public class MuteCommand extends DiscordCommand  {

    public MuteCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Member member, Optional<String> reason) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        message.getGuild().flatMapMany(Guild::getRoles).collectList().subscribe(roles -> {
            Optional<Role> optionalMutedRole = roles.stream().filter(x -> StringUtils.equalsIgnoreCase(x.getName(), "Muted")).findFirst();
            if (!optionalMutedRole.isPresent()) {
                commandManager.sendResponse(message.getChannel(), "Not set up", "The mute command is not yet set up. Please run `.setupmute` to set up the role and channel overrides.").subscribe();
            } else {
                member.addRole(optionalMutedRole.get().getId(), reason.orElse("Member was muted")).subscribe();
                commandManager.sendResponse(message.getChannel(), "Member muted", member.getNicknameMention() + " has been muted.").subscribe();
            }
        });
    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Mutes a member";
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
