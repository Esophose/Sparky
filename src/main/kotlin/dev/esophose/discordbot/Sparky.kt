package dev.esophose.discordbot

import dev.esophose.discordbot.database.DatabaseConnector
import dev.esophose.discordbot.database.SQLiteConnector
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.manager.DataMigrationManager
import dev.esophose.discordbot.manager.GuildSettingsManager
import dev.esophose.discordbot.manager.ListenerManager
import dev.esophose.discordbot.manager.Manager
import dev.esophose.discordbot.manager.PaginatedEmbedManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import io.github.cdimascio.dotenv.Dotenv
import reactor.core.scheduler.Schedulers
import java.io.File
import java.util.HashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object Sparky {

    lateinit var discord: GatewayDiscordClient
    lateinit var self: User
    lateinit var botInfo: ApplicationInfo
    lateinit var connector: DatabaseConnector
    private val managers: MutableMap<KClass<out Manager>, Manager> = HashMap()

    @JvmStatic
    fun main(args: Array<String>) {
        val dotenv = Dotenv.load()
        val token = dotenv["TOKEN"]
        if (token == null) {
            println("Failed to load token from .env")
            exitProcess(1)
        }

        this.discord = DiscordClient.create(token)
                .gateway()
                .setInitialStatus { Presence.doNotDisturb(Activity.watching("the bot start up...")) }
                .setEventDispatcher(EventDispatcher.buffering())
                .setEnabledIntents(IntentSet.all())
                .login()
                .block()!!

        this.discord.on(ReadyEvent::class.java).subscribe {
            this.self = this.discord.self.blockOptional().get()
            this.botInfo = this.discord.applicationInfo.blockOptional().get()

            println("Started as ${this.self.username}#${this.self.discriminator}")

            // Display servers we are in
            this.discord.guilds
                    .map { x -> "${x.name} | ID: ${x.id.asString()} | Members: ${x.memberCount}" }
                    .subscribe { println(it) }

            // Update presence
            Schedulers.parallel().schedulePeriodically({
                BotUtils.watchingUserCount
                        .flatMap { amount -> this.discord.updatePresence(Presence.doNotDisturb(Activity.watching("$amount members | .help"))) }
                        .subscribe()
            }, 1, 30, TimeUnit.SECONDS)
        }

        // Set up database
        val directory = File(System.getProperty("user.dir"))
        this.connector = SQLiteConnector(directory, "sparky")

        // Pre-load managers
        this.getManager(ListenerManager::class)
        this.getManager(DataMigrationManager::class)
        this.getManager(GuildSettingsManager::class)
        this.getManager(CommandManager::class)
        this.getManager(PaginatedEmbedManager::class)

        this.discord.onDisconnect().block()
    }

    fun <M : Manager> getManager(managerClass: KClass<M>): M {
        synchronized(this.managers) {
            @Suppress("UNCHECKED_CAST")
            if (this.managers.containsKey(managerClass))
                return this.managers[managerClass] as M

            return try {
                val manager = managerClass.constructors.first().call()
                manager.enable()
                this.managers[managerClass] = manager
                manager
            } catch (ex: ReflectiveOperationException) {
                error("Failed to load manager for ${managerClass.simpleName}")
            }
        }
    }

}
