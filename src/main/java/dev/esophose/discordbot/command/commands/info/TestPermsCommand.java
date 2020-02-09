package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;

public class TestPermsCommand extends DiscordCommand {

    public TestPermsCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getAuthor().flatMap(Member::getBasePermissions).subscribe(permissions -> {
            PermissionSet missingPermissions = PermissionSet.all().andNot(permissions);
            if (permissions.contains(Permission.ADMINISTRATOR)) {
                commandManager.sendResponse(message.getChannel(), "You're an administrator.", "Congrats, or something.").subscribe();
            } else if (missingPermissions.size() == 1) {
                commandManager.sendResponse(message.getChannel(), "You have all permissions other than administrator.", "So close to greatness.").subscribe();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n**Missing permissions: **").append('\n');
                for (Permission permission : missingPermissions)
                    stringBuilder.append("  - ").append(permission.name()).append('\n');
                commandManager.sendResponse(message.getChannel(), "You are not all powerful", stringBuilder.toString()).subscribe();
            }
        });
    }

    @Override
    public String getName() {
        return "testperms";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Tests your permissions";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.SEND_MESSAGES;
    }

}
