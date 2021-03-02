package dev.esophose.discordbot.listener

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.manager.GuildSettingsManager
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.guild.MemberUpdateEvent
import discord4j.core.event.domain.role.RoleDeleteEvent
import reactor.core.publisher.Mono

class AutoRoleListener : Listener<Event>(MemberJoinEvent::class, MemberUpdateEvent::class, RoleDeleteEvent::class) {

    private var guildSettingsManager: GuildSettingsManager? = null

    override fun execute(event: Event) {
        if (this.guildSettingsManager == null)
            this.guildSettingsManager = Sparky.getManager(GuildSettingsManager::class)

        when (event) {
            is MemberJoinEvent -> executeMemberJoin(event)
            is MemberUpdateEvent -> executeMemberUpdate(event)
            is RoleDeleteEvent -> executeRoleDelete(event)
        }
    }

    private fun grantRole(guildId: Snowflake, member: Member) {
        val autoRoleIds = this.guildSettingsManager!!.getGuildSettings(guildId).autoRoleIds
        if (autoRoleIds.isNotEmpty()) {
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

    private fun executeMemberJoin(event: MemberJoinEvent) {
        if (!event.member.isBot && !event.member.isPending)
            this.grantRole(event.guildId, event.member)
    }

    private fun executeMemberUpdate(event: MemberUpdateEvent) {
        if (event.old.isEmpty || !event.old.get().isPending)
            return

        event.member.subscribe { member ->
            if (!member.isPending)
                this.grantRole(event.guildId, member)
        }
    }

    private fun executeRoleDelete(event: RoleDeleteEvent) {
        val autoRoleIds = this.guildSettingsManager!!.getGuildSettings(event.guildId).autoRoleIds
        if (autoRoleIds.contains(event.roleId))
            this.guildSettingsManager!!.removeAutoRole(event.guildId, event.roleId)
    }

}
