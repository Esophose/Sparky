package dev.esophose.discordbot.command

import discord4j.core.`object`.reaction.ReactionEmoji
import java.util.ArrayList

class DiscordCommandModule(val name: String, private val modulePackage: String, val icon: ReactionEmoji) {

    val loadedCommands: MutableList<DiscordCommand>

    val targetPackage: String
        get() = PACKAGE_ROOT + this.modulePackage

    init {
        this.loadedCommands = ArrayList()
    }

    fun addLoadedCommand(command: DiscordCommand) {
        this.loadedCommands.add(command)
    }

    companion object {
        private const val PACKAGE_ROOT = "dev.esophose.discordbot.command.commands."
    }

}
