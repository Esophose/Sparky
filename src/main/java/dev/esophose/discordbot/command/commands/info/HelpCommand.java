package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.command.DiscordCommandModule;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.manager.PaginatedEmbedManager;
import dev.esophose.discordbot.misc.GuildSettings;
import discord4j.core.object.entity.Member;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HelpCommand extends DiscordCommand {

    public HelpCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        message.getAuthor().flatMap(Member::getBasePermissions).subscribe(permissions -> {
            PaginatedEmbedManager paginatedEmbedManager = this.bot.getManager(PaginatedEmbedManager.class);
            CommandManager commandManager = this.bot.getManager(CommandManager.class);
            GuildSettings guildSettings = this.bot.getManager(GuildSettingsManager.class).getGuildSettings(message.getGuildId());

            if (!commandManager.canAccessCommands(message.getGuildId(), permissions)) {
                commandManager.sendResponse(message.getChannel(), "No access", "You don't have access to use any commands!").subscribe();
                return;
            }

            paginatedEmbedManager.createPaginatedEmbed(message.getAuthorId(), message.getChannelId(), builder -> {
                List<DiscordCommandModule> commandModules = commandManager.getCommandModules();
                for (DiscordCommandModule module : commandModules) {
                    List<DiscordCommand> commands = module.getLoadedCommands();
                    if (commands.stream().noneMatch(x -> permissions.contains(x.getRequiredMemberPermission(message.getGuildId()))))
                        continue;

                    builder.addPage(embed -> {
                        Optional<Unicode> emoji = module.getIcon().asUnicodeEmoji();
                        if (emoji.isPresent()) {
                            embed.setTitle(emoji.get().getRaw() + "  " + module.getName() + " Commands");
                        } else {
                            embed.setTitle(module.getName() + " Commands");
                        }

                        StringBuilder description = new StringBuilder();
                        for (DiscordCommand command : commands)
                            description.append(commandManager.getCommandUsage(command, true, guildSettings.getCommandPrefix())).append(" - ").append(command.getDescription()).append("\n\n");
                        embed.setDescription(description.toString());
                        embed.setColor(guildSettings.getEmbedColor());
                        embed.setFooter("Page %currentPage%/%maxPage%", null);
                    });
                }
            }).subscribe();
        });
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Displays commands you can access";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_MESSAGES, Permission.ADD_REACTIONS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.SEND_MESSAGES;
    }

}
