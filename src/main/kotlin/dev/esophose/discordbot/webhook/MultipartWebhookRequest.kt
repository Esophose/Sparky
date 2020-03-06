package dev.esophose.discordbot.webhook

import reactor.util.annotation.Nullable
import reactor.util.function.Tuple2
import java.io.InputStream

class MultipartWebhookRequest constructor(@get:Nullable val executeRequest: WebhookExecuteRequest,
                                          val files: List<Tuple2<String, InputStream>> = emptyList())
