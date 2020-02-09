package dev.esophose.discordbot.command.commands.moderation;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class MassRoleCommand extends DiscordCommand {

    public MassRoleCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Role role) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        if (role.isEveryone()) {
            commandManager.sendResponse(message.getChannel(), "Don't be silly", "You can't add a role for `@everyone`... They already have that by default.").subscribe();
            return;
        }

        message.getGuild().flatMap(guild -> guild.getMemberById(this.bot.getSelf().getId()))
                .flatMap(Member::getHighestRole)
                .subscribe(highestRole -> highestRole.getPosition()
                        .zipWith(role.getPosition())
                        .subscribe(tuple -> {
                            if (tuple.getT1() <= tuple.getT2()) {
                                commandManager.sendResponse(message.getChannel(), "Role hierarchy issue", "My highest role is " + highestRole.getMention() + " but " + role.getMention() + " has a lower role position. Move the desired mass role below my highest role then try this command again.").subscribe();
                                return;
                            }

                            message.getGuild().flatMapMany(Guild::getMembers)
                                    .filter(x -> !x.isBot())
                                    .filter(x -> !x.getRoleIds().contains(role.getId()))
                                    .count()
                                    .subscribe(amount -> {
                                        if (amount == 0) {
                                            commandManager.sendResponse(message.getChannel(), "That was quick", "All non-bot members in this guild already have " + role.getMention() + " added to their roles.").subscribe();
                                            return;
                                        }

                                        commandManager.sendResponse(message.getChannel(), "Adding roles", "Started granting " + role.getMention() + " to " + amount + " members. This may take a while for large guilds.").subscribe();
                                        message.getGuild().flatMapMany(Guild::getMembers)
                                                .filter(x -> !x.isBot())
                                                .filter(x -> !x.getRoleIds().contains(role.getId()))
                                                .flatMap(x -> x.addRole(role.getId()))
                                                .subscribe();
                                    });
                        }));
    }

    @Override
    public String getName() {
        return "massrole";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Gives a role to all non-bot guild members";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MANAGE_MESSAGES;
    }

}
