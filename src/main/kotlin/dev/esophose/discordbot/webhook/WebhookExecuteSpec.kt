package dev.esophose.discordbot.webhook

import discord4j.core.`object`.Embed
import discord4j.core.`object`.entity.Attachment
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.Spec
import discord4j.discordjson.json.EmbedData
import reactor.util.annotation.Nullable
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.io.InputStream
import java.util.ArrayList

/**
 * Spec used to create [Messages][Message] to [TextChannels][TextChannel]. Clients using this spec must
 * have connected to gateway at least once.
 *
 * @see [Create Message](https://discordapp.com/developers/docs/resources/channel.create-message)
 */
class WebhookExecuteSpec : Spec<MultipartWebhookRequest> {

    @Nullable
    private var content: String? = null
    @Nullable
    private var username: String? = null
    @Nullable
    private var avatarUrl: String? = null
    private var tts: Boolean = false
    private var embed: EmbedData? = null
    private var files: MutableList<Tuple2<String, InputStream>>? = null

    /**
     * Sets the created [Message] contents, up to 2000 characters.
     *
     * @param content The message contents.
     * @return This spec.
     */
    fun setContent(content: String): WebhookExecuteSpec {
        this.content = content
        return this
    }

    /**
     * Sets the webhook execution username
     *
     * @param username The username
     * @return This spec.
     */
    fun setUsername(username: String): WebhookExecuteSpec {
        this.username = username
        return this
    }

    /**
     * Sets the webhook avatar url
     *
     * @param avatarUrl The url
     * @return This spec.
     */
    fun setAvatarUrl(avatarUrl: String): WebhookExecuteSpec {
        this.avatarUrl = avatarUrl
        return this
    }

    /**
     * Sets whether the created [Message] is a TTS message.
     *
     * @param tts If this message is a TTS message.
     * @return This spec.
     */
    fun setTts(tts: Boolean): WebhookExecuteSpec {
        this.tts = tts
        return this
    }

    /**
     * Sets rich content to the created [Message] in the form of an [Embed] object.
     *
     * @param spec An [EmbedCreateSpec] consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    fun setEmbed(spec: (EmbedCreateSpec) -> Unit): WebhookExecuteSpec {
        val mutatedSpec = EmbedCreateSpec()
        spec(mutatedSpec)
        this.embed = mutatedSpec.asRequest()
        return this
    }

    /**
     * Adds a file as attachment to the created [Message].
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    fun addFile(fileName: String, file: InputStream): WebhookExecuteSpec {
        if (this.files == null) {
            this.files = ArrayList(1) // most common case is only 1 attachment per message
        }
        this.files!!.add(Tuples.of(fileName, file))
        return this
    }

    /**
     * Adds a spoiler file as attachment to the created [Message].
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    fun addFileSpoiler(fileName: String, file: InputStream): WebhookExecuteSpec {
        return this.addFile(Attachment.SPOILER_PREFIX + fileName, file)
    }

    override fun asRequest(): MultipartWebhookRequest {
        val json = WebhookExecuteRequest(this.content, this.username, this.avatarUrl, this.tts, this.embed)
        return if (this.files == null) {
            MultipartWebhookRequest(json, emptyList())
        } else {
            MultipartWebhookRequest(json, this.files!!)
        }
    }
}
