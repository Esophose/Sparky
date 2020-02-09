package dev.esophose.discordbot.manager;

import dev.esophose.discordbot.Sparky;

public abstract class Manager {

    protected final Sparky bot;

    public Manager(Sparky bot) {
        this.bot = bot;
    }

    public abstract void enable();

}
