package dev.esophose.discordbot.webhook;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class MultipartWebhookRequest {

    private final WebhookExecuteRequest executeRequest;
    private final List<Tuple2<String, InputStream>> files;

    public MultipartWebhookRequest(WebhookExecuteRequest executeRequest) {
        this(executeRequest, Collections.emptyList());
    }

    public MultipartWebhookRequest(WebhookExecuteRequest executeRequest, String fileName, InputStream file) {
        this(executeRequest, Collections.singletonList(Tuples.of(fileName, file)));
    }

    public MultipartWebhookRequest(WebhookExecuteRequest executeRequest, List<Tuple2<String, InputStream>> files) {
        this.executeRequest = executeRequest;
        this.files = files;
    }

    @Nullable
    public WebhookExecuteRequest getExecuteRequest() {
        return this.executeRequest;
    }

    public List<Tuple2<String, InputStream>> getFiles() {
        return this.files;
    }
}
