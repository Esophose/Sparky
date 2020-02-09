package dev.esophose.discordbot;

import dev.esophose.discordbot.database.DatabaseConnector;
import dev.esophose.discordbot.database.SQLiteConnector;
import dev.esophose.discordbot.manager.CommandManager;
import dev.esophose.discordbot.manager.DataMigrationManager;
import dev.esophose.discordbot.manager.GuildSettingsManager;
import dev.esophose.discordbot.manager.ListenerManager;
import dev.esophose.discordbot.manager.Manager;
import dev.esophose.discordbot.manager.PaginatedEmbedManager;
import dev.esophose.discordbot.utils.BotUtils;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import reactor.core.scheduler.Schedulers;

public class Sparky {

    private static Sparky INSTANCE;

    private GatewayDiscordClient discord;
    private User self;
    private ApplicationInfo botInfo;
    private DatabaseConnector connector;

    private final Map<Class<? extends Manager>, Manager> managers;

    private Sparky() {
        INSTANCE = this;

        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("TOKEN");
        if (token == null) {
            System.err.println("Failed to load token from .env");
            System.exit(1);
        }

        this.discord = DiscordClientBuilder.create(token)
                .build()
                .gateway()
                .setInitialPresence(info -> Presence.doNotDisturb(Activity.watching("the bot start up...")))
                .connect()
                .block();

        if (this.discord == null)
            System.exit(1);

        this.discord.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
            this.self = event.getSelf();
            System.out.println("Started as " + this.self.getUsername() + '#' + this.self.getDiscriminator());

            // Display servers we are in
            this.getDiscord().getGuilds()
                    .map(x -> x.getName() + " | " + x.getId().asString())
                    .subscribe(System.out::println);

            // Load bot info
            this.discord.getApplicationInfo().subscribe(info -> this.botInfo = info);

            // Update presence
            Schedulers.parallel().schedulePeriodically(() -> BotUtils.getWatchingUserCount()
                    .flatMap(amount -> this.discord.updatePresence(Presence.doNotDisturb(Activity.watching(amount + " members | .help"))))
                    .subscribe(), 5, 30, TimeUnit.SECONDS);
        });

        this.managers = new HashMap<>();

        // Set up database
        File directory = new File(System.getProperty("user.dir"));
        this.connector = new SQLiteConnector(directory, "sparky");

        // Pre-load managers
        this.getManager(ListenerManager.class);
        this.getManager(DataMigrationManager.class);
        this.getManager(GuildSettingsManager.class);
        this.getManager(CommandManager.class);
        this.getManager(PaginatedEmbedManager.class);

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <M extends Manager> M getManager(Class<M> managerClass) {
        synchronized (this.managers) {
            if (this.managers.containsKey(managerClass))
                return (M) this.managers.get(managerClass);

            try {
                M manager = managerClass.getConstructor(Sparky.class).newInstance(this);
                manager.enable();
                this.managers.put(managerClass, manager);
                return manager;
            } catch (ReflectiveOperationException ex) {
                return null; // This should never happen
            }
        }
    }

    public GatewayDiscordClient getDiscord() {
        return this.discord;
    }

    public User getSelf() {
        return this.self;
    }

    public ApplicationInfo getBotInfo() {
        return this.botInfo;
    }

    public DatabaseConnector getConnector() {
        return this.connector;
    }

    public static Sparky getInstance() {
        return INSTANCE;
    }

    public static void main(String... args) {
        new Sparky();
    }

}
