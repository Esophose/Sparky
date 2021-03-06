package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.guild.GuildEvent

class GuildJoinLeaveListener : Listener<GuildEvent>(GuildCreateEvent::class, GuildDeleteEvent::class) {

    private var guildSettingsManager: GuildSettingsManager? = null

    override fun execute(event: GuildEvent) {
        if (this.guildSettingsManager == null)
            this.guildSettingsManager = Sparky.getManager(GuildSettingsManager::class)

        when (event) {
            is GuildCreateEvent -> executeCreate(event)
            is GuildDeleteEvent -> executeDelete(event)
        }
    }

    private fun executeCreate(event: GuildCreateEvent) {
        this.guildSettingsManager!!.loadGuildSettings(event.guild.id)
    }

    private fun executeDelete(event: GuildDeleteEvent) {
        this.guildSettingsManager!!.unloadGuildSettings(event.guildId)
    }

}