package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.role.RoleDeleteEvent
import reactor.core.publisher.Mono

class AutoRoleListener : Listener<Event>(MemberJoinEvent::class.java, RoleDeleteEvent::class.java) {

    private var guildSettingsManager: GuildSettingsManager? = null

    override fun execute(event: Event) {
        if (this.guildSettingsManager == null)
            this.guildSettingsManager = Sparky.getManager(GuildSettingsManager::class)

        if (event is MemberJoinEvent) {
            this.executeMemberJoin(event)
        } else if (event is RoleDeleteEvent) {
            this.executeRoleDelete(event)
        }
    }

    private fun executeMemberJoin(event: MemberJoinEvent) {
        if (event.member.isBot)
            return

        val autoRoleIds = this.guildSettingsManager!!.getGuildSettings(event.guildId).autoRoleIds
        if (autoRoleIds.isNotEmpty()) {
            val member = event.member
            var mono: Mono<Void>? = null
            for (roleId in autoRoleIds) {
                mono = if (mono == null) {
                    member.addRole(roleId)
                } else {
                    mono.then(member.addRole(roleId))
                }
            }
            mono?.subscribe()
        }
    }

    private fun executeRoleDelete(event: RoleDeleteEvent) {
        val autoRoleIds = this.guildSettingsManager!!.getGuildSettings(event.guildId).autoRoleIds
        if (autoRoleIds.contains(event.roleId))
            this.guildSettingsManager!!.removeAutoRole(event.guildId, event.roleId)
    }

}
