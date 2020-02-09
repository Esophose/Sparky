package dev.esophose.discordbot.command.commands.moderation;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

public class SetupMuteCommand extends DiscordCommand  {

    public SetupMuteCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        Mono.zip(message.getGuild(), message.getGuild().flatMapMany(Guild::getRoles).collectList()).subscribe(tuple -> {
            Guild guild = tuple.getT1();
            List<Role> roles = tuple.getT2();

            Optional<Role> optionalMutedRole = roles.stream().filter(x -> StringUtils.equalsIgnoreCase(x.getName(), "Muted")).findFirst();
            if (optionalMutedRole.isPresent()) {
                this.applyChannelRoleOverrides(message, guild, optionalMutedRole.get(), false);
            } else {
                guild.createRole(spec -> {
                    spec.setHoist(false);
                    spec.setMentionable(false);
                    spec.setName("Muted");
                    spec.setPermissions(PermissionSet.none());
                    spec.setReason("Setting up bot muted role");
                }).subscribe(role -> this.applyChannelRoleOverrides(message, guild, role, true));
            }
        });
    }

    private void applyChannelRoleOverrides(DiscordCommandMessage message, Guild guild, Role mutedRole, boolean createdRole) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        Snowflake roleId = mutedRole.getId();
        guild.getChannels().flatMap(channel -> {
            Optional<ExtendedPermissionOverwrite> optionalOverwrite = channel.getOverwriteForRole(roleId);
            if (optionalOverwrite.isPresent())
                return Mono.empty();

            if (channel instanceof TextChannel) {
                return channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId, PermissionSet.none(), PermissionSet.of(Permission.SEND_MESSAGES)));
            } else if (channel instanceof VoiceChannel) {
                return channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId, PermissionSet.none(), PermissionSet.of(Permission.SPEAK)));
            } else if (channel instanceof Category) {
                return channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId, PermissionSet.none(), PermissionSet.of(Permission.SEND_MESSAGES, Permission.SPEAK)));
            }

            return Mono.empty();
        }).hasElements().flatMap(x -> commandManager.sendResponse(message.getChannel(), "Set up muted command", "The muted command has been set up.")).subscribe();
    }

    @Override
    public String getName() {
        return "setupmute";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Sets up the .mute command for all channels";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.MANAGE_CHANNELS, Permission.MANAGE_ROLES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MUTE_MEMBERS;
    }

}
