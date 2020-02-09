package dev.esophose.discordbot.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.rest.json.request.EmbedRequest;
import java.util.ArrayList;
import java.util.List;
import reactor.util.annotation.Nullable;

public class WebhookExecuteRequest {

    @Nullable
    private final String content;
    @Nullable
    private final String username;
    @Nullable
    @JsonProperty("avatar_url")
    private final String avatarUrl;
    private final boolean tts;
    @Nullable
    private final List<EmbedRequest> embeds = new ArrayList<>();

    public WebhookExecuteRequest(@Nullable String content, @Nullable String username,
                                 @Nullable String avatarUrl, boolean tts, @Nullable EmbedRequest embed) {
        this.content = content;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.tts = tts;

        if (embed != null)
            this.embeds.add(embed);
    }

    @Override
    public String toString() {
        return "MessageCreateRequest{" +
                "content='" + this.content + '\'' +
                ", username='" + this.username + '\'' +
                ", avatar_url='" + this.avatarUrl + '\'' +
                ", tts=" + this.tts +
                ", embeds=" + this.embeds +
                '}';
    }
}
