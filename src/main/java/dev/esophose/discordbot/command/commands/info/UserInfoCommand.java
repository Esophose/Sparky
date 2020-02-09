package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.WordUtils;
import reactor.core.publisher.Mono;

public class UserInfoCommand extends DiscordCommand {

    public UserInfoCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Optional<Member> member) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        member.map(Mono::just)
                .orElseGet(message::getAuthor)
                .subscribe(target -> {
                    Mono.zip(target.getHighestRole().switchIfEmpty(message.getGuild().flatMap(Guild::getEveryoneRole)), target.getColor(), target.getPresence(), target.getRoles().collectList())
                            .subscribe(tuple -> {
                                String roles = tuple.getT4().stream().sorted(Comparator.comparingInt(Role::getRawPosition).reversed()).map(Role::getMention).collect(Collectors.joining(" "));
                                if (roles.isBlank())
                                    roles = "None";

                                String info =
                                        "**Snowflake:** " + target.getId().asString() + '\n' +
                                                "**Tag:** " + target.getMention() + '\n' +
                                                "**Discord Join Time:** " + BotUtils.snowflakeAsDateTimeString(target.getId()) + '\n' +
                                                "**Guild Join Time:** " + BotUtils.formatDateTime(LocalDateTime.ofInstant(target.getJoinTime(), ZoneOffset.UTC)) + '\n' +
                                                "**Main Role:** " + (tuple.getT1().isEveryone() ? "@everyone" : tuple.getT1().getMention()) + '\n' +
                                                "**Color:** " + BotUtils.toHexString(tuple.getT2()) + '\n' +
                                                "**Displayname:** " + target.getDisplayName() + '\n' +
                                                "**Status:** " + WordUtils.capitalize(tuple.getT3().getStatus().getValue()) + '\n' +
                                                "**Presence:** " + BotUtils.presenceAsString(tuple.getT3()) + '\n' +
                                                "**Roles:** " + roles;
                                commandManager.sendResponse(message.getChannel(), "Info for " + target.getUsername() + '#' + target.getDiscriminator(), info, target.getAvatarUrl()).subscribe();
                            });
                });
    }

    @Override
    public String getName() {
        return "userinfo";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("uinfo");
    }

    @Override
    public String getDescription() {
        return "Displays info for a user";
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
