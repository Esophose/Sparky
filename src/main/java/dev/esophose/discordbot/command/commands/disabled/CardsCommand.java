package dev.esophose.discordbot.command.commands.disabled;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.cards.Card;
import dev.esophose.discordbot.cards.Deck;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Flux;

public class CardsCommand extends DiscordCommand {

    public CardsCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message) {
        Flux.fromIterable(new Deck().getCards())
                .flatMap(Card::getEmojiString)
                .collectList()
                .flatMap(emojis -> this.bot.getManager(CommandManager.class).sendResponse(message.getChannel(), "Shuffled Deck of Cards", String.join(" ", emojis)))
                .subscribe();
    }

    @Override
    public String getName() {
        return "cards";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Displays a deck of shuffled cards";
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
