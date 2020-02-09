package dev.esophose.discordbot.command.commands.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class ColorCommand extends DiscordCommand {

    public ColorCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Color color) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild().subscribe(guild -> message.getAuthor().subscribe(member -> member.getRoles().collectList().subscribe(roles -> {
            roles.stream().filter(x -> x.getName().startsWith("Color-#")).forEach(role -> role.delete().subscribe());

            String colorString = BotUtils.toHexString(color);

            guild.createRole(spec -> {
                spec.setName("Color-" + colorString);
                spec.setMentionable(false);
                spec.setHoist(false);
                spec.setColor(color);
                spec.setPermissions(PermissionSet.none());
            }).map(Role::getId).flatMap(member::addRole).subscribe();

            commandManager.sendResponse(message.getChannel(), "Color added", "Your role color has been set to `" + colorString + "`!").subscribe();
        })));
    }

    @Override
    public String getName() {
        return "color";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Changes your name color";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_ROLES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MANAGE_ROLES;
    }

}
