package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

public class FindRolesCommand extends DiscordCommand {

    public FindRolesCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, String input) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild()
                .flatMapMany(Guild::getRoles)
                .filter(x -> input.equals("*") || StringUtils.containsIgnoreCase(x.getName(), input))
                .sort(Comparator.comparingInt(Role::getRawPosition).reversed())
                .collectList()
                .switchIfEmpty(Mono.just(new ArrayList<>()))
                .flatMap(roles -> {
                    if (roles.isEmpty()) {
                        return commandManager.sendResponse(message.getChannel(), "No roles found", "No roles were found matching your input.");
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        roles.forEach(x -> stringBuilder.append(x.getMention()).append(" | ").append(x.getId().asString()).append('\n'));
                        return commandManager.sendResponse(message.getChannel(), roles.size() + " " + (roles.size() > 1 ? "Roles" : "Role") + " found matching \"" + input + "\"", stringBuilder.toString());
                    }
                })
                .subscribe();
    }

    @Override
    public String getName() {
        return "findroles";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("fr");
    }

    @Override
    public String getDescription() {
        return "Finds roles matching the given input";
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
