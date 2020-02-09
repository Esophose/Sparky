package dev.esophose.discordbot.manager

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.listener.AutoRoleListener
import dev.esophose.discordbot.listener.GuildJoinLeaveListener
import dev.esophose.discordbot.listener.Listener
import dev.esophose.discordbot.listener.MemberJoinSecurityListener
import dev.esophose.discordbot.listener.MessageCreateSecurityListener
import dev.esophose.discordbot.listener.PaginatedEmbedListener
import discord4j.core.event.domain.Event

class ListenerManager : Manager() {

    override fun enable() {
        this.registerListener(MemberJoinSecurityListener())
        this.registerListener(MessageCreateSecurityListener())
        this.registerListener(GuildJoinLeaveListener())
        this.registerListener(PaginatedEmbedListener())
        this.registerListener(AutoRoleListener())
    }

    private fun <T : Event> registerListener(listener: Listener<T>) {
        for (eventClass in listener.eventClasses)
            Sparky.discord.eventDispatcher.on(eventClass).subscribe { listener.execute(it) }
    }

}
