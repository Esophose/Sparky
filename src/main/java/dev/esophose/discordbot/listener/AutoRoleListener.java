package dev.esophose.discordbot.listener;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import java.util.List;
import reactor.core.publisher.Mono;

public class AutoRoleListener extends Listener<Event> {

    private GuildSettingsManager guildSettingsManager;

    public AutoRoleListener() {
        super(MemberJoinEvent.class, RoleDeleteEvent.class);
    }

    @Override
    public void execute(Event event) {
        if (this.guildSettingsManager == null)
            this.guildSettingsManager = Sparky.getInstance().getManager(GuildSettingsManager.class);

        if (event instanceof MemberJoinEvent) {
            this.executeMemberJoin((MemberJoinEvent) event);
        } else if (event instanceof RoleDeleteEvent) {
            this.executeRoleDelete((RoleDeleteEvent) event);
        }
    }

    private void executeMemberJoin(MemberJoinEvent event) {
        if (event.getMember().isBot())
            return;

        List<Snowflake> autoRoleIds = this.guildSettingsManager.getGuildSettings(event.getGuildId()).getAutoRoleIds();
        if (!autoRoleIds.isEmpty()) {
            Member member = event.getMember();
            Mono<Void> mono = null;
            for (Snowflake roleId : autoRoleIds) {
                if (mono == null) {
                    mono = member.addRole(roleId);
                } else {
                    mono = mono.then(member.addRole(roleId));
                }
            }
            if (mono != null)
                mono.subscribe();
        }
    }

    private void executeRoleDelete(RoleDeleteEvent event) {
        List<Snowflake> autoRoleIds = this.guildSettingsManager.getGuildSettings(event.getGuildId()).getAutoRoleIds();
        if (autoRoleIds.contains(event.getRoleId()))
            this.guildSettingsManager.removeAutoRole(event.getGuildId(), event.getRoleId());
    }

}
