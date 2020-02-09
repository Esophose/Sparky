package dev.esophose.discordbot.webhook;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.Spec;
import discord4j.rest.json.request.EmbedRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Spec used to create {@link Message Messages} to {@link TextChannel TextChannels}. Clients using this spec must
 * have connected to gateway at least once.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#create-message">Create Message</a>
 */
public class WebhookExecuteSpec implements Spec<MultipartWebhookRequest> {

    @Nullable
    private String content;
    @Nullable
    private String username;
    @Nullable
    private String avatarUrl;
    private boolean tts;
    private EmbedRequest embed;
    private List<Tuple2<String, InputStream>> files;

    /**
     * Sets the created {@link Message} contents, up to 2000 characters.
     *
     * @param content The message contents.
     * @return This spec.
     */
    public WebhookExecuteSpec setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the webhook execution username
     *
     * @param username The username
     * @return This spec.
     */
    public WebhookExecuteSpec setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the webhook avatar url
     *
     * @param avatarUrl The url
     * @return This spec.
     */
    public WebhookExecuteSpec setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    /**
     * Sets whether the created {@link Message} is a TTS message.
     *
     * @param tts If this message is a TTS message.
     * @return This spec.
     */
    public WebhookExecuteSpec setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    /**
     * Sets rich content to the created {@link Message} in the form of an {@link Embed} object.
     *
     * @param spec An {@link EmbedCreateSpec} consumer used to attach rich content when creating a message.
     * @return This spec.
     */
    public WebhookExecuteSpec setEmbed(Consumer<? super EmbedCreateSpec> spec) {
        final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
        spec.accept(mutatedSpec);
        this.embed = mutatedSpec.asRequest();
        return this;
    }

    /**
     * Adds a file as attachment to the created {@link Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public WebhookExecuteSpec addFile(String fileName, InputStream file) {
        if (this.files == null) {
            this.files = new ArrayList<>(1); // most common case is only 1 attachment per message
        }
        this.files.add(Tuples.of(fileName, file));
        return this;
    }

    /**
     * Adds a spoiler file as attachment to the created {@link Message}.
     *
     * @param fileName The filename used in the file being sent.
     * @param file The file contents.
     * @return This spec.
     */
    public WebhookExecuteSpec addFileSpoiler(String fileName, InputStream file) {
        return this.addFile(Attachment.SPOILER_PREFIX + fileName, file);
    }

    @Override
    public MultipartWebhookRequest asRequest() {
        WebhookExecuteRequest json = new WebhookExecuteRequest(this.content, this.username, this.avatarUrl, this.tts, this.embed);
        return new MultipartWebhookRequest(json, this.files == null ? Collections.emptyList() : this.files);
    }
}
