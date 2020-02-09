package dev.esophose.discordbot.command;

import discord4j.core.object.reaction.ReactionEmoji;
import java.util.ArrayList;
import java.util.List;

public class DiscordCommandModule {

    private static final String PACKAGE_ROOT = "dev.esophose.discordbot.command.commands.";

    private String moduleName;
    private String modulePackage;
    private ReactionEmoji moduleIcon;
    private List<DiscordCommand> loadedCommands;

    public DiscordCommandModule(String moduleName, String modulePackage, ReactionEmoji moduleIcon) {
        this.moduleName = moduleName;
        this.modulePackage = modulePackage;
        this.moduleIcon = moduleIcon;
        this.loadedCommands = new ArrayList<>();
    }

    public String getName() {
        return this.moduleName;
    }

    public String getPackage() {
        return PACKAGE_ROOT + this.modulePackage;
    }

    public ReactionEmoji getIcon() {
        return this.moduleIcon;
    }

    public void addLoadedCommand(DiscordCommand command) {
        this.loadedCommands.add(command);
    }

    public List<DiscordCommand> getLoadedCommands() {
        return this.loadedCommands;
    }

}
