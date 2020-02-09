package dev.esophose.discordbot.command.commands.info;

import com.google.common.collect.Lists;
import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public class SecurityAuditCommand extends DiscordCommand {

    public SecurityAuditCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild()
                .flatMapMany(Guild::getMembers)
                .flatMap(Member::getBasePermissions)
                .collectList()
                .flatMap(permissions -> commandManager.sendResponse(message.getChannel(), "User permission amounts", this.getPermissionCountsString(permissions)))
                .subscribe();

        message.getGuild()
                .flatMapMany(Guild::getMembers)
                .collectList()
                .flatMap(members -> Flux.zip(Flux.fromIterable(members), Flux.fromIterable(members).flatMap(Member::getBasePermissions))
                        .collectList()
                        .flatMap(tuple -> commandManager.sendResponse(message.getChannel(), "Administrative users", this.getAdministrativeCountsString(tuple)))
                ).subscribe();
    }

    private String getPermissionCountsString(List<PermissionSet> permissions) {
        Map<Permission, Integer> permissionCounts = new HashMap<>();
        for (PermissionSet permissionSet : permissions) {
            for (Permission permission : permissionSet) {
                if (permissionCounts.containsKey(permission)) {
                    permissionCounts.put(permission, permissionCounts.get(permission) + 1);
                } else {
                    permissionCounts.put(permission, 1);
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        permissionCounts.entrySet()
                .stream()
                .sorted(Comparator.comparingLong(entry -> entry.getKey().getValue()))
                .forEach(entry -> stringBuilder.append(entry.getKey().name()).append(": ").append(entry.getValue()).append('\n'));

        return stringBuilder.toString();
    }

    private String getAdministrativeCountsString(List<Tuple2<Member, PermissionSet>> permissions) {
        StringBuilder stringBuilder = new StringBuilder("The following members have ADMINISTATOR access:\n");
        permissions.stream()
                .filter(x -> x.getT2().contains(Permission.ADMINISTRATOR))
                .filter(x -> !x.getT1().isBot())
                .forEach(x -> stringBuilder.append(x.getT1().getNicknameMention()).append('\n'));

        long botAdminsCount = permissions.stream()
                .filter(x -> x.getT2().contains(Permission.ADMINISTRATOR))
                .filter(x -> x.getT1().isBot())
                .count();

        if (botAdminsCount > 0) {
            stringBuilder.append("\n\n\u26a0 **WARNING! YOU HAVE (")
                    .append(botAdminsCount)
                    .append(") ADMINISTRATIVE BOTS!**")
                    .append("\nBots with administrative permissions are usually a *very bad* idea.")
                    .append("\nYou should restrict these bots to only have the minimum permissions they require:\n");
            permissions.stream()
                    .filter(x -> x.getT2().contains(Permission.ADMINISTRATOR))
                    .filter(x -> x.getT1().isBot())
                    .forEach(x -> stringBuilder.append(x.getT1().getNicknameMention()).append('\n'));
        }

        return stringBuilder.toString();
    }

    @Override
    public String getName() {
        return "securityaudit";
    }

    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("sa", "audit");
    }

    @Override
    public String getDescription() {
        return "Perform a security audit of your guild";
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
