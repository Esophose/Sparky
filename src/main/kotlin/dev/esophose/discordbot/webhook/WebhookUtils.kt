package dev.esophose.discordbot.webhook

import dev.esophose.discordbot.Sparky
import discord4j.core.`object`.entity.Webhook
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.rest.request.Router
import discord4j.rest.route.Routes
import discord4j.rest.service.RestService
import discord4j.rest.util.Image
import reactor.core.publisher.Mono
import java.util.Objects

object WebhookUtils {

    private var router: Router? = null

    init {
        try {
            val method_getRouter = RestService::class.java.getDeclaredMethod("getRouter")
            method_getRouter.isAccessible = true
            router = method_getRouter.invoke(Sparky.discord.restClient.webhookService) as Router
        } catch (e: ReflectiveOperationException) {
            e.printStackTrace()
        }
    }

    fun createAndExecuteWebhook(channel: MessageChannel, username: String, avatar: Image, spec: (WebhookExecuteSpec) -> Unit): Mono<Void> {
        return (channel as TextChannel).createWebhook { webhookCreateSpec ->
            webhookCreateSpec.setName(username)
            webhookCreateSpec.setAvatar(avatar)
        }.flatMap { webhook -> executeWebhook(webhook, spec).then(webhook.delete()) }
    }

    fun createAndExecuteWebhook(channelMono: Mono<MessageChannel>, username: String, avatar: Image, spec: (WebhookExecuteSpec) -> Unit): Mono<Void> {
        return channelMono.cast(TextChannel::class.java).flatMap { channel -> createAndExecuteWebhook(channel, username, avatar, spec) }
    }

    fun executeWebhook(webhook: Webhook, spec: (WebhookExecuteSpec) -> Unit): Mono<Void> {
        val mutatedSpec = WebhookExecuteSpec()
        spec(mutatedSpec)

        val request = mutatedSpec.asRequest()
        return Routes.WEBHOOK_EXECUTE.newRequest(webhook.id.asLong(), webhook.token)
                .header("content-type", if (request.files.isEmpty()) "application/json" else "multipart/form-data")
                .body(Objects.requireNonNull(if (request.files.isEmpty()) request.executeRequest else request))
                .exchange(router!!)
                .bodyToMono(Void::class.java)
    }

}
