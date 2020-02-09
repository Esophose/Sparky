package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class FindChannelsCommand extends DiscordCommand {

    public FindChannelsCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, String input) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        message.getGuild()
                .flatMapMany(Guild::getChannels)
                .filter(x -> input.equals("*") || StringUtils.containsIgnoreCase(x.getName(), input))
                .flatMap(channel -> Mono.zip(Mono.just(channel), channel.getPosition()))
                .sort(Comparator.comparingInt(Tuple2<GuildChannel, Integer>::getT2).reversed())
                .map(Tuple2::getT1)
                .collectList()
                .switchIfEmpty(Mono.just(new ArrayList<>()))
                .flatMap(channels -> {
                    if (channels.isEmpty()) {
                        return commandManager.sendResponse(message.getChannel(), "No channels found", "No channels were found matching your input.");
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        channels.forEach(x -> {
                            if (x instanceof Category) {
                                stringBuilder.append("**").append(x.getMention()).append("**");
                            } else if (x instanceof VoiceChannel) {
                                stringBuilder.append("*").append(x.getMention()).append("*");
                            } else {
                                stringBuilder.append(x.getMention());
                            }
                            stringBuilder.append(" | ").append(x.getId().asString()).append('\n');
                        });
                        return commandManager.sendResponse(message.getChannel(), channels.size() + " " + (channels.size() > 1 ? "Channels" : "Channel") + " found matching \"" + input + "\"", stringBuilder.toString());
                    }
                })
                .subscribe();
    }

    @Override
    public String getName() {
        return "findchannels";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("fc");
    }

    @Override
    public String getDescription() {
        return "Finds channels matching the given input";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_CHANNELS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MANAGE_CHANNELS;
    }

}
