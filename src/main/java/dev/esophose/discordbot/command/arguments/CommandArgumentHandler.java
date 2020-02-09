package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;

public class CommandArgumentHandler extends DiscordCommandArgumentHandler<DiscordCommand> {

    @Override
    protected Mono<DiscordCommand> handleInternal(Guild guild, String input) {
        String prefix = Sparky.getInstance().getManager(GuildSettingsManager.class).getGuildSettings(guild.getId()).getCommandPrefix();
        // Remove prefix if the user included it
        if (input.startsWith(prefix))
            input = input.substring(prefix.length());

        String commandName = input;
        for (DiscordCommand command : Sparky.getInstance().getManager(CommandManager.class).getCommands())
            if (command.getName().equalsIgnoreCase(commandName) || command.getAliases().stream().anyMatch(x -> x.equalsIgnoreCase(commandName)))
                return Mono.just(command);

        return Mono.empty();
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid command: [" + input + "], use **.help** for a list";
    }

    @Override
    public Class<DiscordCommand> getHandledType() {
        return DiscordCommand.class;
    }

}
