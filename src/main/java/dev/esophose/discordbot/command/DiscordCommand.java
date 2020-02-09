package dev.esophose.discordbot.command;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ensure a {@code public void} method named {@code execute} with a first parameter of {@link DiscordCommandMessage} exists.
 * All following parameters after the first must have a matching DiscordCommandArgumentHandler to be valid.
 */
public abstract class DiscordCommand implements Comparable<DiscordCommand> {

    protected final Sparky bot;

    public DiscordCommand(Sparky bot) {
        this.bot = bot;
    }

    public abstract String getName();

    public abstract List<String> getAliases();

    public abstract String getDescription();

    public abstract PermissionSet getRequiredBotPermissions();

    public abstract Permission getDefaultRequiredMemberPermission();

    public Permission getRequiredMemberPermission(Snowflake guildId) {
        return this.bot.getManager(GuildSettingsManager.class)
                .getGuildSettings(guildId)
                .getCommandPermissions()
                .get(this);
    }

    public Method getExecuteMethod() {
        return Stream.of(this.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals("execute"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public List<DiscordCommandArgumentInfo> getArgumentInfo() {
        return Stream.of(this.getParameters())
                .map(DiscordCommandArgumentInfo::new)
                .collect(Collectors.toList());
    }

    public int getNumParameters() {
        return this.getParameters().length - 1;
    }

    public int getNumOptionalParameters() {
        return Math.toIntExact(this.getArgumentInfo().stream().filter(DiscordCommandArgumentInfo::isOptional).count());
    }

    public int getNumRequiredArguments() {
        return this.getNumParameters() - this.getNumOptionalParameters();
    }

    private Parameter[] getParameters() {
        return Stream.of(this.getExecuteMethod().getParameters())
                .skip(1)
                .toArray(Parameter[]::new);
    }

    public int compareTo(DiscordCommand other) {
        return this.getName().compareTo(other.getName());
    }

}
