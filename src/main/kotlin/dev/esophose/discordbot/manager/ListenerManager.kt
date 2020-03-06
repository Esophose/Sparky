package dev.esophose.discordbot.manager

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.listener.*
import discord4j.core.event.domain.Event

class ListenerManager : Manager() {

    override fun enable() {
        this.registerListener(MemberJoinSecurityListener())
        this.registerListener(MessageCreateSecurityListener())
        this.registerListener(GuildJoinLeaveListener())
        this.registerListener(PaginatedEmbedListener())
        this.registerListener(AutoRoleListener())
        this.registerListener(MessageAuditListener())
    }

    private fun <T : Event> registerListener(listener: Listener<T>) {
        for (eventClass in listener.eventClasses)
            Sparky.discord.eventDispatcher.on(eventClass.java).subscribe { listener.execute(it) }
    }

}
