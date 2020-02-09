package dev.esophose.discordbot.command.commands.setting;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.misc.GuildSettings;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class AddAutoRoleCommand extends DiscordCommand {

    public AddAutoRoleCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Role role) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        GuildSettingsManager guildSettingsManager = this.bot.getManager(GuildSettingsManager.class);

        if (role.isEveryone()) {
            commandManager.sendResponse(message.getChannel(), "Don't be silly", "You can't add an auto role for `@everyone`... They already have that by default.").subscribe();
            return;
        }

        GuildSettings guildSettings = guildSettingsManager.getGuildSettings(message.getGuildId());
        if (guildSettings.getAutoRoleIds().contains(role.getId())) {
            commandManager.sendResponse(message.getChannel(), "Auto role already exists", "An auto role for " + role.getMention() + " already exists.").subscribe();
            return;
        }

        message.getGuild().flatMap(guild -> guild.getMemberById(this.bot.getSelf().getId()))
                .flatMap(Member::getHighestRole)
                .subscribe(highestRole -> highestRole.getPosition()
                        .zipWith(role.getPosition())
                        .subscribe(tuple -> {
                            if (tuple.getT1() <= tuple.getT2()) {
                                commandManager.sendResponse(message.getChannel(), "Auto role hierarchy issue", "My highest role is " + highestRole.getMention() + " but " + role.getMention() + " has a lower role position. Move the desired auto role below my highest role then try this command again.").subscribe();
                                return;
                            }

                            guildSettingsManager.addAutoRole(message.getGuildId(), role.getId());
                            commandManager.sendResponse(message.getChannel(), "Added auto role", "An auto role for " + role.getMention() + " has been added.").subscribe();
                        }));
    }

    @Override
    public String getName() {
        return "addautorole";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Adds a role to be automatically given to new members on join";
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
