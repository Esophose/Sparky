package dev.esophose.discordbot.command.commands.setting;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.misc.GuildSettings;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;

public class SettingsCommand extends DiscordCommand {

    public SettingsCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);
        GuildSettings guildSettings = this.bot.getManager(GuildSettingsManager.class).getGuildSettings(message.getGuildId());

        message.getGuild().flatMapMany(guild -> Flux.fromIterable(guildSettings.getAutoRoleIds()).flatMap(guild::getRoleById)).collectList().flatMap(autoRoles -> {
            StringBuilder commandPermissions = new StringBuilder("**Command permissions**\n");
            for (DiscordCommand command : commandManager.getCommands()) {
                Permission permission = command.getRequiredMemberPermission(message.getGuildId());
                commandPermissions.append(guildSettings.getCommandPrefix())
                        .append(command.getName())
                        .append(" - ")
                        .append(permission.name())
                        .append("\n");
            }

            StringBuilder autoRolesString = new StringBuilder("Auto roles: ");
            if (autoRoles.isEmpty()) {
                autoRolesString.append("`None`");
            } else {
                autoRolesString.append("[")
                        .append(autoRoles.stream().map(Role::getMention).collect(Collectors.joining(", ")))
                        .append("]");
            }

            String info = "These are the current bot settings for this guild:\n\n" +
                    "Command prefix: `" + guildSettings.getCommandPrefix() + "`\n\n" +
                    "Embed color: `" + BotUtils.toHexString(guildSettings.getEmbedColor()) + "`\n\n" +
                    "Member join security: `" + (guildSettings.isMemberJoinSecurityEnabled() ? "enabled" : "disabled") + "`\n\n" +
                    "Message creation security: `" + (guildSettings.isMessageCreateSecurityEnabled() ? "enabled" : "disabled") + "`\n\n" +
                    autoRolesString + "\n\n" +
                    commandPermissions;

            return commandManager.sendResponse(message.getChannel(), "Bot settings", info);
        }).subscribe();
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "View the bot settings for this guild";
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
