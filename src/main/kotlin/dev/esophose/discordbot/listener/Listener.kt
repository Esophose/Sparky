package dev.esophose.discordbot.listener

import discord4j.core.event.domain.Event
import java.util.Arrays
import java.util.stream.Collectors
import kotlin.streams.toList

abstract class Listener<T : Event>(vararg eventClasses: Class<*>) {

    val eventClasses: List<Class<T>>

    init {
        @Suppress("UNCHECKED_CAST")
        this.eventClasses = Arrays.stream(eventClasses).map { x -> x as Class<T> }.toList()
    }

    abstract fun execute(event: T)

}
