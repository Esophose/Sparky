package dev.esophose.discordbot.webhook

import com.fasterxml.jackson.annotation.JsonProperty
import discord4j.discordjson.json.EmbedData
import reactor.util.annotation.Nullable
import java.util.ArrayList

class WebhookExecuteRequest(@param:Nullable @field:Nullable private val content: String?,
                            @param:Nullable @field:Nullable private val username: String?,
                            @param:Nullable @field:Nullable @field:JsonProperty("avatar_url") private val avatarUrl: String?,
                            private val tts: Boolean,
                            @Nullable embed: EmbedData?) {
    @Nullable
    private val embeds = ArrayList<EmbedData>()

    init {
        if (embed != null)
            this.embeds.add(embed)
    }

    override fun toString(): String {
        return "MessageCreateRequest{" +
                "content='" + this.content + '\'' +
                ", username='" + this.username + '\'' +
                ", avatar_url='" + this.avatarUrl + '\'' +
                ", tts=" + this.tts +
                ", embeds=" + this.embeds +
                '}'
    }
}
