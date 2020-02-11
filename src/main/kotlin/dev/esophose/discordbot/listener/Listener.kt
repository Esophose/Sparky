package dev.esophose.discordbot.listener

import discord4j.core.event.domain.Event
import java.util.Arrays
import java.util.stream.Collectors
import kotlin.reflect.KClass
import kotlin.streams.toList

abstract class Listener<T : Event>(vararg eventClasses: KClass<*>) {

    val eventClasses: List<KClass<T>>

    init {
        @Suppress("UNCHECKED_CAST")
        this.eventClasses = Arrays.stream(eventClasses).map { it as KClass<T> }.toList()
    }

    abstract fun execute(event: T)

}
