package dev.esophose.discordbot.command.arguments

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandArgumentHandler
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class CommandArgumentHandler : DiscordCommandArgumentHandler<DiscordCommand>() {

    override val handledType: KClass<DiscordCommand>
        get() = DiscordCommand::class

    override fun handleInternal(guild: Guild, input: String): Mono<DiscordCommand> {
        val prefix = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(guild.id).commandPrefix
        // Remove prefix if the user included it
        var commandName: String = input
        if (input.startsWith(prefix))
            commandName = input.substring(prefix.length)

        for (command in Sparky.getManager(CommandManager::class).commands)
            if (command.name.equals(commandName, ignoreCase = true) || command.aliases.stream().anyMatch { x -> x.equals(commandName, ignoreCase = true) })
                return Mono.just(command)

        return Mono.empty()
    }

    override fun getErrorMessage(guild: Guild, input: String): String {
        return "Invalid command: [$input], use **.help** for a list"
    }

}
