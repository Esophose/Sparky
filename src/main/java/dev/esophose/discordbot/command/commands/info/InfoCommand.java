package dev.esophose.discordbot.command.commands.info;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.misc.GuildSettings;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

public class InfoCommand extends DiscordCommand {

    public InfoCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        Mono.zip(this.bot.getDiscord().getGuilds().count(),
                BotUtils.getWatchingUserCount()).subscribe(tuple -> {
            User self = this.bot.getSelf();
            long guildCount = tuple.getT1();
            long userCount = tuple.getT2();
            GuildSettings guildSettings = this.bot.getManager(GuildSettingsManager.class).getGuildSettings(message.getGuildId());
            String prefix = guildSettings.getCommandPrefix();

            String info = "Hi, my name is " + self.getUsername() + "!\n" +
                    "I'm a utility bot written with Discord4J 3.1.\n" +
                    "Currently, I'm watching over " + guildCount + " guilds with a total of " + userCount + " members.\n" +
                    "My prefix for this guild is `" + prefix + "`\n" +
                    "If you'd like to know more about what I can do, try out my `" + prefix + "help` command.";

            this.bot.getBotInfo().getOwner().flatMap(owner -> message.getChannel()
                    .flatMap(channel -> channel.createEmbed(spec -> {
                        spec.setTitle("Bot Info");
                        spec.setDescription(info);
                        spec.setThumbnail(self.getAvatarUrl());
                        spec.setFooter("Bot Creator: " + owner.getUsername() + "#" + owner.getDiscriminator(), owner.getAvatarUrl());
                        spec.setColor(guildSettings.getEmbedColor());
                        spec.setUrl("https://discordapp.com/api/oauth2/authorize?client_id=323964742538625025&permissions=2113793271&scope=bot");
                    }))).subscribe();
        });
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Displays information about the bot";
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
