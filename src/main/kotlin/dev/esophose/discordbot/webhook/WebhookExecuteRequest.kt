package dev.esophose.discordbot.webhook

import com.fasterxml.jackson.annotation.JsonProperty
import discord4j.rest.json.request.EmbedRequest
import java.util.ArrayList
import reactor.util.annotation.Nullable

class WebhookExecuteRequest(@param:Nullable @field:Nullable private val content: String?,
                            @param:Nullable @field:Nullable private val username: String?,
                            @param:Nullable @field:Nullable @field:JsonProperty("avatar_url") private val avatarUrl: String?,
                            private val tts: Boolean,
                            @Nullable embed: EmbedRequest?) {
    @Nullable
    private val embeds = ArrayList<EmbedRequest>()

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
