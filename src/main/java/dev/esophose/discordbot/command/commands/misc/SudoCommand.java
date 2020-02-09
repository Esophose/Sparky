package dev.esophose.discordbot.command.commands.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.webhook.WebhookUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

public class SudoCommand extends DiscordCommand {

    public SudoCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Member member, String command) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        String commandPrefix = this.bot.getManager(GuildSettingsManager.class).getGuildSettings(message.getGuildId()).getCommandPrefix();
        member.getAvatar().flatMap(avatar -> WebhookUtils.createAndExecuteWebhook(message.getChannel(), member.getDisplayName(), avatar, spec -> {
            spec.setContent(command);
        })).thenEmpty(Mono.fromRunnable(() -> {
            if (command.startsWith(commandPrefix))
                commandManager.executeCommand(message.getGuild(), message.getChannel(), message.getChannelId(), message.getMessageId(), member, command, commandPrefix);
        })).subscribe();
    }

    @Override
    public String getName() {
        return "sudo";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("force");
    }

    @Override
    public String getDescription() {
        return "Performs a command as another member";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_WEBHOOKS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.ADMINISTRATOR;
    }

}
