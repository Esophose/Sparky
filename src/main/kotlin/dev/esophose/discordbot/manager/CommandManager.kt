package dev.esophose.discordbot.manager

import com.google.common.reflect.ClassPath
import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.*
import dev.esophose.discordbot.command.arguments.EnumArgumentHandler
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.awt.Color
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.reflect.KClass

class CommandManager : Manager() {

    private val argumentHandlers: MutableMap<KClass<out DiscordCommandArgumentHandler<*>>, DiscordCommandArgumentHandler<*>>
    val commandModules: List<DiscordCommandModule>
    private val commandLookupMap: MutableMap<String, DiscordCommand>

    val commands: List<DiscordCommand>
        get() {
            val commands = ArrayList<DiscordCommand>()
            this.commandModules.stream().map { it.loadedCommands }.forEach { commands.addAll(it) }
            commands.sort()
            return commands
        }

    init {
        this.commandLookupMap = HashMap()
        this.argumentHandlers = HashMap()
        this.commandModules = listOf(
                DiscordCommandModule("Info", "info", ReactionEmoji.unicode("\u2757")),
                DiscordCommandModule("Setting", "setting", ReactionEmoji.unicode("\uD83D\uDEE0")),
                DiscordCommandModule("Moderation", "moderation", ReactionEmoji.unicode("\uD83D\uDEA8")),
                DiscordCommandModule("Misc", "misc", ReactionEmoji.unicode("\uD83C\uDF1F"))
        )

        Sparky.discord.eventDispatcher.on(MessageCreateEvent::class.java).subscribe { this.handleMessageCreation(it) }
    }

    @Suppress("UnstableApiUsage")
    override fun enable() {
        try {
            for (module in this.commandModules) {
                for (classInfo in ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses(module.targetPackage)) {
                    val command = classInfo.load().getConstructor().newInstance() as DiscordCommand
                    module.addLoadedCommand(command)
                    this.commandLookupMap[command.name.toLowerCase()] = command
                    command.aliases.forEach { x -> this.commandLookupMap[x.toLowerCase()] = command }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        try {
            for (classInfo in ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses("dev.esophose.discordbot.command.arguments")) {
                val argumentHandler = classInfo.load().getConstructor().newInstance() as DiscordCommandArgumentHandler<*>
                this.argumentHandlers[argumentHandler::class] = argumentHandler
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    fun getArgumentHandler(handledParameterType: Class<*>): DiscordCommandArgumentHandler<*>? {
        return if (handledParameterType.isEnum) this.argumentHandlers[EnumArgumentHandler::class] else this.argumentHandlers.values
                .stream()
                .filter { x -> x.handledType == handledParameterType.kotlin }
                .findFirst()
                .orElseThrow<IllegalStateException> { IllegalStateException() }
    }

    fun handleMessageCreation(event: MessageCreateEvent) {
        val optionalMember = event.member
        if (optionalMember.isEmpty)
            return

        val member = optionalMember.get()
        if (member.isBot)
            return

        val optionalContent = event.message.content
        if (optionalContent.isEmpty)
            return

        val optionalGuildId = event.guildId
        if (optionalGuildId.isEmpty)
            return

        val content = optionalContent.get()
        val commandPrefix = Sparky.getManager(GuildSettingsManager::class).getGuildSettings(optionalGuildId.get()).commandPrefix
        if (!content.startsWith(commandPrefix)) {
            // Force bot mentions that are at the beginning of a message to trigger the info command
            event.guild.flatMap { x -> x.getMemberById(Sparky.self.id) }.subscribe {
                if (content.startsWith(it.mention) || content.startsWith(it.nicknameMention))
                    this.executeCommand(event.guild, event.message.channel, event.message.channelId, event.message.id, member, commandPrefix + "info", commandPrefix)
            }
            return
        }

        this.executeCommand(event.guild, event.message.channel, event.message.channelId, event.message.id, member, content, commandPrefix)
    }

    fun executeCommand(guildMono: Mono<Guild>, channelMono: Mono<MessageChannel>, channelId: Snowflake, messageId: Snowflake, member: Member, content: String, commandPrefix: String?) {
        try {
            val matcher = COMMAND_PATTERN.matcher(content.substring(commandPrefix!!.length))
            val pieces = ArrayList<String>()
            while (matcher.find())
                pieces.add(matcher.group(1).replace("\"", ""))

            if (pieces.isEmpty())
                return

            val commandName = pieces[0].toLowerCase()
            val command = this.getCommand(commandName) ?: return

            if (command.numRequiredArguments > pieces.size - 1) {
                this.sendResponse(channelMono, "Missing arguments", this.getCommandUsage(command, true, commandPrefix)).subscribe()
                return
            }

            guildMono.subscribe { guild ->
                Mono.zip(member.basePermissions, guild.getMemberById(Sparky.self.id).flatMap { it.basePermissions })
                        .subscribe { permissions ->
                            val requiredMemberPermission = command.getRequiredMemberPermission(guild.id)
                            val hasMemberPermission = permissions.t1.contains(requiredMemberPermission) || permissions.t1.contains(Permission.ADMINISTRATOR)
                            var missingBotPermissions = command.requiredBotPermissions.andNot(permissions.t2)

                            if (permissions.t2.contains(Permission.ADMINISTRATOR))
                                missingBotPermissions = PermissionSet.none()

                            if (!hasMemberPermission || !missingBotPermissions.isEmpty()) {
                                val stringBuilder = StringBuilder()
                                if (!hasMemberPermission) {
                                    stringBuilder.append("\n**Missing Member Permission: **").append('\n')
                                    stringBuilder.append("  - ").append(requiredMemberPermission.name).append('\n')
                                }

                                if (!missingBotPermissions.isEmpty()) {
                                    stringBuilder.append("\n**Missing Bot Permissions: **").append('\n')
                                    for (permission in missingBotPermissions)
                                        stringBuilder.append("  - ").append(permission.name).append('\n')
                                }

                                this.sendResponse(channelMono, "Missing permissions", stringBuilder.toString()).subscribe()
                            } else {
                                val argumentInfo = command.argumentInfo
                                val combinedArguments = ArrayList<Tuple3<DiscordCommandArgumentInfo, DiscordCommandArgumentHandler<*>, String>>()
                                for (i in argumentInfo.indices) {
                                    val argInfo = argumentInfo[i]
                                    val input: String = when {
                                        i + 1 >= pieces.size -> ""
                                        i == argumentInfo.size - 1 -> pieces.stream().skip((i + 1).toLong()).collect(Collectors.joining(" "))
                                        else -> pieces[i + 1]
                                    }
                                    combinedArguments.add(Tuples.of(argInfo, this.getArgumentHandler(argInfo.type)!!, input))
                                }

                                Flux.fromIterable(combinedArguments)
                                        .filterWhen { x ->
                                            if (x.t1.isEnum)
                                                (x.t2 as EnumArgumentHandler).currentHandledType = x.t1.type.kotlin

                                            x.t2.isInvalid(guild, x.t3, x.t1.isOptional)
                                        }
                                        .collectList()
                                        .subscribe { invalidArgs ->
                                            if (invalidArgs.isEmpty()) {
                                                Flux.fromIterable(combinedArguments)
                                                        .flatMap { x -> x.t2.handle(guild, x.t3, x.t1.isOptional) }
                                                        .collectList()
                                                        .subscribe { parsedArgs ->
                                                            val commandMessage = DiscordCommandMessage(guild.id, channelId, messageId, member.id)
                                                            val argumentBuilder = Stream.builder<Any>().add(commandMessage)
                                                            for (parsedArg in parsedArgs)
                                                                argumentBuilder.add(parsedArg)

                                                            try {
                                                                command.executeMethod.invoke(command, *argumentBuilder.build().toArray())
                                                            } catch (e: ReflectiveOperationException) {
                                                                e.printStackTrace()
                                                            }
                                                        }
                                            } else {
                                                val stringBuilder = StringBuilder()
                                                invalidArgs.forEach { tuple ->
                                                    stringBuilder.append("**")
                                                            .append(tuple.t1.name)
                                                            .append(" \u2192** ")
                                                            .append(tuple.t2.getErrorMessage(guild, tuple.t3))
                                                            .append('\n')
                                                }

                                                stringBuilder.append('\n').append("**Correct Usage:** ").append(this.getCommandUsage(command, true, commandPrefix))
                                                this.sendResponse(channelMono, "Invalid argument(s)", stringBuilder.toString()).subscribe()
                                            }
                                        }
                            }
                        }
            }
        } catch (ex: Exception) {
            this.sendResponse(channelMono, "An unknown error occurred", ex.message ?: "No other details available").subscribe()
        }

    }

    fun getCommandUsage(command: DiscordCommand?, includeCommandName: Boolean, commandPrefix: String?): String {
        val stringBuilder = StringBuilder()

        if (includeCommandName)
            stringBuilder.append(commandPrefix).append(command!!.name)

        for (argumentInfo in command!!.argumentInfo) {
            if (argumentInfo.isOptional) {
                stringBuilder.append(" [").append(argumentInfo.name).append("]")
            } else {
                stringBuilder.append(" <").append(argumentInfo.name).append(">")
            }
        }

        return stringBuilder.toString()
    }

    fun sendResponse(channelMono: Mono<MessageChannel>, title: String?, response: String): Mono<Message> {
        return this.sendResponse(channelMono, title, response, null)
    }

    fun sendResponse(channelMono: Mono<MessageChannel>, title: String?, response: String, thumbnailUrl: String?): Mono<Message> {
        return channelMono.cast(TextChannel::class.java)
                .flatMap { channel -> channel.createEmbed { spec -> this.applyEmbedSpec(channel.guildId, spec, title, response, thumbnailUrl) } }
    }

    fun applyEmbedSpec(guildId: Snowflake, spec: EmbedCreateSpec, title: String?, response: String, thumbnailUrl: String?): EmbedCreateSpec {
        if (title != null) spec.setTitle(title)
        spec.setColor(Sparky.getManager(GuildSettingsManager::class).getGuildSettings(guildId).embedColor)
        spec.setDescription(response)

        if (thumbnailUrl != null)
            spec.setThumbnail(thumbnailUrl)

        return spec
    }

    fun getCommand(commandName: String): DiscordCommand? {
        return this.commandLookupMap[commandName]
    }

    fun canAccessCommands(guildId: Snowflake, permissions: PermissionSet): Boolean {
        for (command in this.commands)
            if (permissions.contains(command.getRequiredMemberPermission(guildId)))
                return true
        return false
    }

    companion object {
        const val DEFAULT_PREFIX = "."
        val DEFAULT_EMBED_COLOR = Color(0x24bdc1) // 0xefca04, 0xe96b9a
        val COMMAND_PATTERN: Pattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*")
    }

}
