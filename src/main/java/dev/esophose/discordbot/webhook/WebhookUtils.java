package dev.esophose.discordbot.webhook;

import dev.esophose.discordbot.Sparky;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.util.Image;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.service.RestService;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;

public final class WebhookUtils {

    private static Router router;

    static {
        try {
            Method method_getRouter = RestService.class.getDeclaredMethod("getRouter");
            method_getRouter.setAccessible(true);
            router = (Router) method_getRouter.invoke(Sparky.getInstance().getDiscord().getCoreResources().getRestClient().getWebhookService());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static Mono<Void> createAndExecuteWebhook(MessageChannel channel, String username, Image avatar, Consumer<? super WebhookExecuteSpec> spec) {
        return ((TextChannel) channel).createWebhook(webhookCreateSpec -> {
            webhookCreateSpec.setName(username);
            webhookCreateSpec.setAvatar(avatar);
        }).flatMap(webhook -> executeWebhook(webhook, spec).then(webhook.delete()));
    }

    public static Mono<Void> createAndExecuteWebhook(Mono<MessageChannel> channelMono, String username, Image avatar, Consumer<? super WebhookExecuteSpec> spec) {
        return channelMono.cast(TextChannel.class).flatMap(channel -> createAndExecuteWebhook(channel, username, avatar, spec));
    }

    public static Mono<Void> executeWebhook(Webhook webhook, Consumer<? super WebhookExecuteSpec> spec) {
        WebhookExecuteSpec mutatedSpec = new WebhookExecuteSpec();
        spec.accept(mutatedSpec);

        MultipartWebhookRequest request = mutatedSpec.asRequest();
        return Routes.WEBHOOK_EXECUTE.newRequest(webhook.getId().asLong(), webhook.getToken())
                .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
                .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getExecuteRequest() : request))
                .exchange(router)
                .bodyToMono(Void.class);
    }

}
