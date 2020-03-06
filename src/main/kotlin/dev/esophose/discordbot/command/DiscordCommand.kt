package dev.esophose.discordbot.command

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import discord4j.core.`object`.util.Snowflake
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Ensure a `public void` method named `execute` with a first parameter of [DiscordCommandMessage] exists.
 * All following parameters after the first must have a matching DiscordCommandArgumentHandler to be valid.
 */
abstract class DiscordCommand(val botOwnerOnly: Boolean = false) : Comparable<DiscordCommand> {

    abstract val name: String

    abstract val aliases: List<String>

    abstract val description: String

    abstract val requiredBotPermissions: PermissionSet

    abstract val defaultRequiredMemberPermission: Permission

    val executeMethod: Method
        get() = Stream.of(*this::class.java.declaredMethods)
                .filter { it.name == "execute" }
                .findFirst()
                .orElseThrow { IllegalStateException() }

    val argumentInfo: List<DiscordCommandArgumentInfo>
        get() = Stream.of(*this.parameters)
                .map { DiscordCommandArgumentInfo(it) }
                .toList()

    val numParameters: Int
        get() = this.parameters.size - 1

    val numOptionalParameters: Int
        get() = Math.toIntExact(this.argumentInfo.stream().filter { it.isOptional }.count())

    val numRequiredArguments: Int
        get() = this.numParameters - this.numOptionalParameters

    private val parameters: Array<Parameter>
        get() = Stream.of(*this.executeMethod.parameters)
                .skip(1)
                .toArray<Parameter> { arrayOfNulls(it) }

    fun getRequiredMemberPermission(guildId: Snowflake): Permission {
        return Sparky.getManager(GuildSettingsManager::class)
                .getGuildSettings(guildId)
                .commandPermissions[this]!!
    }

    override fun compareTo(other: DiscordCommand): Int {
        return this.name.compareTo(other.name)
    }

}
