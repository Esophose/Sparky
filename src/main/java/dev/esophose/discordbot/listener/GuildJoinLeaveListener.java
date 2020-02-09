package dev.esophose.discordbot.listener;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.GuildEvent;

public class GuildJoinLeaveListener extends Listener<GuildEvent> {

    private GuildSettingsManager guildSettingsManager;

    public GuildJoinLeaveListener() {
        super(GuildCreateEvent.class, GuildDeleteEvent.class);
    }

    @Override
    public void execute(GuildEvent event) {
        if (this.guildSettingsManager == null)
            this.guildSettingsManager = Sparky.getInstance().getManager(GuildSettingsManager.class);

        if (event instanceof GuildCreateEvent) {
            this.executeCreate((GuildCreateEvent) event);
        } else if (event instanceof GuildDeleteEvent) {
            this.executeDelete((GuildDeleteEvent) event);
        }
    }

    private void executeCreate(GuildCreateEvent event) {
        this.guildSettingsManager.loadGuildSettings(event.getGuild().getId());
    }

    private void executeDelete(GuildDeleteEvent event) {
        this.guildSettingsManager.unloadGuildSettings(event.getGuildId());
    }

}