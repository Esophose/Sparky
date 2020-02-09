package dev.esophose.discordbot.manager;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import dev.esophose.discordbot.command.DiscordCommandArgumentInfo;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.command.DiscordCommandModule;
import dev.esophose.discordbot.command.arguments.EnumArgumentHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class CommandManager extends Manager {

    public static final String DEFAULT_PREFIX = ".";
    public static final Color DEFAULT_EMBED_COLOR = new Color(0xefca04); // 0xe96b9a
    public static final Pattern COMMAND_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    private Map<Class<? extends DiscordCommandArgumentHandler>, DiscordCommandArgumentHandler<?>> argumentHandlers;
    private List<DiscordCommandModule> commandModules;
    private Map<String, DiscordCommand> commandLookupMap;

    public CommandManager(Sparky bot) {
        super(bot);

        this.commandLookupMap = new HashMap<>();
        this.argumentHandlers = new HashMap<>();
        this.commandModules = Arrays.asList(
                new DiscordCommandModule("Info", "info", ReactionEmoji.unicode("\u2757")),
                new DiscordCommandModule("Setting", "setting", ReactionEmoji.unicode("\uD83D\uDEE0")),
                new DiscordCommandModule("Moderation", "moderation", ReactionEmoji.unicode("\uD83D\uDEA8")),
                new DiscordCommandModule("Misc", "misc", ReactionEmoji.unicode("\uD83C\uDF1F"))
        );

        this.bot.getDiscord().getEventDispatcher().on(MessageCreateEvent.class).subscribe(this::handleMessageCreation);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void enable() {
        try {
            for (DiscordCommandModule module : this.commandModules) {
                for (ClassInfo classInfo : ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses(module.getPackage())) {
                    DiscordCommand command = (DiscordCommand) classInfo.load().getConstructor(Sparky.class).newInstance(this.bot);
                    module.addLoadedCommand(command);
                    this.commandLookupMap.put(command.getName().toLowerCase(), command);
                    command.getAliases().forEach(x -> this.commandLookupMap.put(x.toLowerCase(), command));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            for (ClassInfo classInfo : ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses("dev.esophose.discordbot.command.arguments")) {
                DiscordCommandArgumentHandler<?> argumentHandler = (DiscordCommandArgumentHandler<?>) classInfo.load().getConstructor().newInstance();
                this.argumentHandlers.put(argumentHandler.getClass(), argumentHandler);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DiscordCommandArgumentHandler<?> getArgumentHandler(Class<?> handledParameterType) {
        if (Enum.class.isAssignableFrom(handledParameterType))
            return this.argumentHandlers.get(EnumArgumentHandler.class);

        return this.argumentHandlers.values()
                .stream()
                .filter(x -> x.getHandledType() != null && x.getHandledType().equals(handledParameterType))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public void handleMessageCreation(MessageCreateEvent event) {
        Optional<Member> optionalMember = event.getMember();
        if (optionalMember.isEmpty())
            return;

        Member member = optionalMember.get();
        if (member.isBot())
            return;

        Optional<String> optionalContent = event.getMessage().getContent();
        if (optionalContent.isEmpty())
            return;

        Optional<Snowflake> optionalGuildId = event.getGuildId();
        if (optionalGuildId.isEmpty())
            return;

        String content = optionalContent.get();
        String commandPrefix = this.bot.getManager(GuildSettingsManager.class).getGuildSettings(optionalGuildId.get()).getCommandPrefix();
        if (!content.startsWith(commandPrefix)) {
            // Force bot mentions that are at the beginning of a message to trigger the info command
            event.getGuild().flatMap(x -> x.getMemberById(this.bot.getSelf().getId())).subscribe(self -> {
                if (content.startsWith(self.getMention()) || content.startsWith(self.getNicknameMention()))
                    this.executeCommand(event.getGuild(), event.getMessage().getChannel(), event.getMessage().getChannelId(), event.getMessage().getId(), member, commandPrefix + "info", commandPrefix);
            });
            return;
        }

        this.executeCommand(event.getGuild(), event.getMessage().getChannel(), event.getMessage().getChannelId(), event.getMessage().getId(), member, content, commandPrefix);
    }

    @SuppressWarnings("unchecked")
    public void executeCommand(Mono<Guild> guildMono, Mono<MessageChannel> channelMono, Snowflake channelId, Snowflake messageId, Member member, String content, String commandPrefix) {
        try {
            content = content.substring(commandPrefix.length());
            Matcher matcher = COMMAND_PATTERN.matcher(content);
            List<String> pieces = new ArrayList<>();
            while (matcher.find())
                pieces.add(matcher.group(1).replace("\"", ""));

            if (pieces.isEmpty())
                return;

            String commandName = pieces.get(0).toLowerCase();
            DiscordCommand command = this.getCommand(commandName);
            if (command == null)
                return; // No command found

            if (command.getNumRequiredArguments() > pieces.size() - 1) {
                this.sendResponse(channelMono, "Missing arguments", this.getCommandUsage(command, true, commandPrefix)).subscribe();
                return;
            }

            guildMono.subscribe(guild -> Mono.zip(member.getBasePermissions(), guild.getMemberById(this.bot.getSelf().getId()).flatMap(Member::getBasePermissions))
                    .subscribe(permissions -> {
                        Permission requiredMemberPermission = command.getRequiredMemberPermission(guild.getId());
                        boolean hasMemberPermission = permissions.getT1().contains(requiredMemberPermission) || permissions.getT1().contains(Permission.ADMINISTRATOR);
                        PermissionSet missingBotPermissions = command.getRequiredBotPermissions().andNot(permissions.getT2());

                        if (permissions.getT2().contains(Permission.ADMINISTRATOR))
                            missingBotPermissions = PermissionSet.none();

                        if (!hasMemberPermission || !missingBotPermissions.isEmpty()) {
                            StringBuilder stringBuilder = new StringBuilder();
                            if (!hasMemberPermission) {
                                stringBuilder.append("\n**Missing Member Permission: **").append('\n');
                                stringBuilder.append("  - ").append(requiredMemberPermission.name()).append('\n');
                            }

                            if (!missingBotPermissions.isEmpty()) {
                                stringBuilder.append("\n**Missing Bot Permissions: **").append('\n');
                                for (Permission permission : missingBotPermissions)
                                    stringBuilder.append("  - ").append(permission.name()).append('\n');
                            }

                            this.sendResponse(channelMono, "Missing permissions", stringBuilder.toString()).subscribe();
                            return;
                        }

                        List<DiscordCommandArgumentInfo> argumentInfo = command.getArgumentInfo();
                        List<Tuple3<DiscordCommandArgumentInfo, DiscordCommandArgumentHandler<?>, String>> combinedArguments = new ArrayList<>();
                        for (int i = 0; i < argumentInfo.size(); i++) {
                            DiscordCommandArgumentInfo argInfo = argumentInfo.get(i);
                            String input;
                            if (i + 1 >= pieces.size()) {
                                input = "";
                            } else if (i == argumentInfo.size() - 1) {
                                input = pieces.stream().skip(i + 1).collect(Collectors.joining(" "));
                            } else {
                                input = pieces.get(i + 1);
                            }
                            combinedArguments.add(Tuples.of(argInfo, this.getArgumentHandler(argInfo.getType()), input));
                        }

                        Flux.fromIterable(combinedArguments)
                                .filterWhen(x -> {
                                    if (x.getT1().isEnum())
                                        ((EnumArgumentHandler)x.getT2()).setHandledType(x.getT1().getType());
                                    return x.getT2().isInvalid(guild, x.getT3(), x.getT1().isOptional());
                                })
                                .collectList()
                                .subscribe(invalidArgs -> {
                                    if (invalidArgs.isEmpty()) {
                                        Flux.fromIterable(combinedArguments)
                                                .flatMap(x -> x.getT2().handle(guild, x.getT3(), x.getT1().isOptional()))
                                                .collectList()
                                                .subscribe(parsedArgs -> {
                                                    DiscordCommandMessage commandMessage = new DiscordCommandMessage(guild.getId(), channelId, messageId, member.getId());
                                                    Stream.Builder<Object> argumentBuilder = Stream.builder().add(commandMessage);
                                                    for (Object parsedArg : parsedArgs)
                                                        argumentBuilder.add(parsedArg);

                                                    try {
                                                        command.getExecuteMethod().invoke(command, argumentBuilder.build().toArray());
                                                    } catch (ReflectiveOperationException e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                    } else {
                                        StringBuilder stringBuilder = new StringBuilder();
                                        invalidArgs.forEach(tuple -> stringBuilder.append("**")
                                                .append(tuple.getT1().getName())
                                                .append(" \u2192** ")
                                                .append(tuple.getT2().getErrorMessage(guild, tuple.getT3()))
                                                .append('\n'));

                                        stringBuilder.append('\n').append("**Correct Usage:** ").append(this.getCommandUsage(command, true, commandPrefix));
                                        this.sendResponse(channelMono, "Invalid argument(s)", stringBuilder.toString()).subscribe();
                                    }
                                });
                    }));
        } catch (Exception ex) {
            this.sendResponse(channelMono, "An unknown error occurred", ex.getMessage()).subscribe();
        }
    }

    public String getCommandUsage(DiscordCommand command, boolean includeCommandName, String commandPrefix) {
        StringBuilder stringBuilder = new StringBuilder();

        if (includeCommandName)
            stringBuilder.append(commandPrefix).append(command.getName());

        for (DiscordCommandArgumentInfo argumentInfo : command.getArgumentInfo()) {
            if (argumentInfo.isOptional()) {
                stringBuilder.append(" [").append(argumentInfo.getName()).append("]");
            } else {
                stringBuilder.append(" <").append(argumentInfo.getName()).append(">");
            }
        }

        return stringBuilder.toString();
    }

    public Mono<Message> sendResponse(Mono<MessageChannel> channelMono, String title, String response) {
        return this.sendResponse(channelMono, title, response, null);
    }

    public Mono<Message> sendResponse(Mono<MessageChannel> channelMono, String title, String response, String thumbnailUrl) {
        return channelMono.cast(TextChannel.class)
                .flatMap(channel -> channel.createEmbed(spec -> this.applyEmbedSpec(channel.getGuildId(), spec, title, response, thumbnailUrl)));
    }

    public EmbedCreateSpec applyEmbedSpec(Snowflake guildId, EmbedCreateSpec spec, String title, String response, String thumbnailUrl) {
        if (title != null) spec.setTitle(title);
        spec.setColor(this.bot.getManager(GuildSettingsManager.class).getGuildSettings(guildId).getEmbedColor());
        spec.setDescription(response);

        if (thumbnailUrl != null)
            spec.setThumbnail(thumbnailUrl);

        return spec;
    }

    public DiscordCommand getCommand(String commandName) {
        return this.commandLookupMap.get(commandName);
    }

    public List<DiscordCommand> getCommands() {
        List<DiscordCommand> commands = new ArrayList<>();
        this.commandModules.stream().map(DiscordCommandModule::getLoadedCommands).forEach(commands::addAll);
        Collections.sort(commands);
        return commands;
    }

    public List<DiscordCommandModule> getCommandModules() {
        return this.commandModules;
    }

    public boolean canAccessCommands(Snowflake guildId, PermissionSet permissions) {
        for (DiscordCommand command : this.getCommands())
            if (permissions.contains(command.getRequiredMemberPermission(guildId)))
                return true;
        return false;
    }

}
