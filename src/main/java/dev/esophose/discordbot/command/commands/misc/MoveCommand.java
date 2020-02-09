package dev.esophose.discordbot.command.commands.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.utils.BotUtils;
import dev.esophose.discordbot.webhook.WebhookUtils;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

public class MoveCommand extends DiscordCommand {

    public MoveCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Snowflake messageId, GuildChannel textChannel) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        if (!(textChannel instanceof GuildMessageChannel)) {
            commandManager.sendResponse(message.getChannel(), "Invalid text channel", "The target channel must be a text channel.").subscribe();
            return;
        }

        GuildMessageChannel targetChannel = (GuildMessageChannel) textChannel;
        message.getChannel().flatMap(channel -> channel.getMessageById(messageId).doOnError(error -> commandManager.sendResponse(message.getChannel(), "Failed to move message", "The given message snowflake was invalid.").subscribe())).subscribe(targetMessage -> {
            Mono.zip(targetMessage.getAuthorAsMember(), targetMessage.getAuthorAsMember().flatMap(Member::getAvatar))
                    .flatMap(specDetails -> WebhookUtils.createAndExecuteWebhook(targetChannel, specDetails.getT1().getDisplayName(), specDetails.getT2(), spec -> {
                        targetMessage.getContent().ifPresent(spec::setContent);
                        targetMessage.getAttachments().forEach(file -> spec.addFile(file.getFilename(), BotUtils.getAttachment(file.getUrl())));
                        if (!targetMessage.getEmbeds().isEmpty()) {
                            Embed embed = targetMessage.getEmbeds().get(0);
                            spec.setEmbed(embedSpec -> {
                                embed.getAuthor().ifPresent(author -> embedSpec.setAuthor(author.getName(), author.getUrl(), author.getIconUrl()));
                                embed.getColor().ifPresent(embedSpec::setColor);
                                embed.getDescription().ifPresent(embedSpec::setDescription);
                                embed.getFields().forEach(field -> embedSpec.addField(field.getName(), field.getValue(), field.isInline()));
                                embed.getFooter().ifPresent(footer -> embedSpec.setFooter(footer.getText(), footer.getIconUrl()));
                                embed.getImage().map(Embed.Image::getUrl).ifPresent(embedSpec::setUrl);
                                embed.getThumbnail().map(Embed.Thumbnail::getUrl).ifPresent(embedSpec::setThumbnail);
                                embed.getTimestamp().ifPresent(embedSpec::setTimestamp);
                                embed.getTitle().ifPresent(embedSpec::setTitle);
                                embed.getUrl().ifPresent(embedSpec::setUrl);
                            });
                        }
                    })).doOnSuccess(v -> {
                targetMessage.delete("Moved message").subscribe();
                commandManager.sendResponse(message.getChannel(), "Message moved", "The message was moved successfully.").subscribe();
            }).subscribe();
        });
    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Moves a message from one channel to another";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES, Permission.MANAGE_CHANNELS, Permission.MANAGE_MESSAGES, Permission.MANAGE_WEBHOOKS);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.MANAGE_MESSAGES;
    }

}
