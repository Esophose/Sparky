package dev.esophose.discordbot.listener;

import discord4j.core.event.domain.Event;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Listener<T extends Event> {

    private final List<Class<T>> eventClasses;

    @SuppressWarnings("unchecked")
    public Listener(Class<?>... eventClasses) {
        this.eventClasses = Arrays.stream(eventClasses).map(x -> (Class<T>) x).collect(Collectors.toList());
    }

    public List<Class<T>> getEventClasses() {
        return this.eventClasses;
    }

    public abstract void execute(T event);

}
