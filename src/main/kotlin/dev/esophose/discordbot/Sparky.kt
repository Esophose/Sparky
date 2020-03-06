package dev.esophose.discordbot

import dev.esophose.discordbot.database.DatabaseConnector
import dev.esophose.discordbot.database.SQLiteConnector
import dev.esophose.discordbot.manager.*
import dev.esophose.discordbot.utils.BotUtils
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.lifecycle.ReadyEvent
import io.github.cdimascio.dotenv.Dotenv
import reactor.core.scheduler.Schedulers
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object Sparky {

    val discord: GatewayDiscordClient
        get() = discord_!!
    val self: User
        get() = self_!!
    val botInfo: ApplicationInfo
        get() = botInfo_!!
    val connector: DatabaseConnector
        get() = connector_!!

    private var discord_: GatewayDiscordClient? = null
    private var self_: User? = null
    private var botInfo_: ApplicationInfo? = null
    private var connector_: DatabaseConnector? = null
    private val managers: MutableMap<KClass<out Manager>, Manager> = HashMap()

    private fun start() {
        val dotenv = Dotenv.load()
        val token = dotenv["TOKEN"]
        if (token == null) {
            println("Failed to load token from .env")
            exitProcess(1)
        }

        this.discord_ = DiscordClientBuilder.create(token)
                .build()
                .gateway()
                .setInitialPresence { Presence.doNotDisturb(Activity.watching("the bot start up...")) }
                .connect()
                .blockOptional()
                .get()

        this.self_ = this.discord.self.blockOptional().get()
        this.botInfo_ = this.discord.applicationInfo.blockOptional().get()

        this.discord.eventDispatcher.on(ReadyEvent::class.java).subscribe {
            println("Started as ${this.self.username}#${this.self.discriminator}")

            // Display servers we are in
            this.discord.guilds
                    .map { x -> x.name + " | " + x.id.asString() }
                    .subscribe { println(it) }

            // Update presence
            Schedulers.parallel().schedulePeriodically({
                BotUtils.watchingUserCount
                        .flatMap { amount -> this.discord.updatePresence(Presence.doNotDisturb(Activity.watching("$amount members | .help"))) }
                        .subscribe()
            }, 5, 30, TimeUnit.SECONDS)
        }

        // Set up database
        val directory = File(System.getProperty("user.dir"))
        this.connector_ = SQLiteConnector(directory, "sparky")

        // Pre-load managers
        this.getManager(ListenerManager::class)
        this.getManager(DataMigrationManager::class)
        this.getManager(GuildSettingsManager::class)
        this.getManager(CommandManager::class)
        this.getManager(PaginatedEmbedManager::class)
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

    @JvmStatic
    fun main(args: Array<String>) {
        start()

        // Wait for program to be forcefully terminated
        try {
            Thread.currentThread().join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}
