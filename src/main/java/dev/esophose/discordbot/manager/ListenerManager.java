package dev.esophose.discordbot.manager;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.listener.AutoRoleListener;
import dev.esophose.discordbot.listener.GuildJoinLeaveListener;
import dev.esophose.discordbot.listener.Listener;
import dev.esophose.discordbot.listener.MemberJoinSecurityListener;
import dev.esophose.discordbot.listener.MessageCreateSecurityListener;
import dev.esophose.discordbot.listener.PaginatedEmbedListener;
import discord4j.core.event.domain.Event;

public class ListenerManager extends Manager {

    public ListenerManager(Sparky bot) {
        super(bot);
    }

    @Override
    public void enable() {
        this.registerListener(new MemberJoinSecurityListener());
        this.registerListener(new MessageCreateSecurityListener());
        this.registerListener(new GuildJoinLeaveListener());
        this.registerListener(new PaginatedEmbedListener());
        this.registerListener(new AutoRoleListener());
    }

    public <T extends Event> void registerListener(Listener<T> listener) {
        for (Class<T> eventClass : listener.getEventClasses())
            this.bot.getDiscord().getEventDispatcher().on(eventClass).subscribe(listener::execute);
    }

}
