package dev.esophose.discordbot.utils;

import dev.esophose.discordbot.Sparky;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import reactor.core.publisher.Mono;

public final class BotUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    public static String presenceAsString(Presence presence) {
        Optional<Activity> optionalActivity = presence.getActivity();
        if (optionalActivity.isEmpty())
            return "Doing nothing";

        Activity activity = optionalActivity.get();
        switch (activity.getType()) {
            case PLAYING:
                return "Playing " + activity.getName();
            case STREAMING:
                return "Streaming " + activity.getName();
            case LISTENING:
                return "Listening to " + activity.getName();
            case WATCHING:
                return "Watching " + activity.getName();
            case CUSTOM:
                return activity.getState().orElse(activity.getName());
            default:
                return "Doing nothing";
        }
    }

    public static String snowflakeAsDateTimeString(Snowflake snowflake) {
        long millis = (snowflake.asLong() / 4194304) + 1420070400000L;
        return formatDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    public static InputStream getAttachment(String attachmentURL) {
        try {
            URL url = new URL(attachmentURL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36");
            return connection.getInputStream();
        } catch (IOException e) {
            return new InputStream() {
                @Override
                public int read() {
                    return -1;
                }
            };
        }
    }

    public static Mono<Long> getWatchingUserCount() {
        return Sparky.getInstance().getDiscord().getUsers().filter(x -> !x.isBot()).distinct().count();
    }

    public static String toHexString(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

}
