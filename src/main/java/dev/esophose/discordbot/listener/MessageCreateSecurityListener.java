package dev.esophose.discordbot.listener;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;

public class MessageCreateSecurityListener extends Listener<MessageCreateEvent> {

    private final static int DANGER_THRESHOLD = 30; // 30 messages
    private final static int PRUNE_TIME = 1000 * 10; // 10 seconds
    private final static int DANGER_UPDATE_THRESHOLD = 1000 * 60 * 10; // 10 minutes
    private final static int MESSAGE_RATE_LIMIT = 30; // 30 seconds

    private final Map<TextChannel, List<AuditedEvent>> auditedEvents;
    private final Set<TextChannel> dangerModes;

    public MessageCreateSecurityListener() {
        super(MessageCreateEvent.class);

        this.auditedEvents = Collections.synchronizedMap(new HashMap<>());
        this.dangerModes = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public void execute(MessageCreateEvent event) {
        if (event.getMember().isEmpty() || event.getMember().get().isBot())
            return;

        Mono.zip(event.getGuild(), event.getMessage().getChannel()).subscribe(monos -> {
            if (!(monos.getT2() instanceof TextChannel))
                return;

            CommandManager commandManager = Sparky.getInstance().getManager(CommandManager.class);
            Guild guild = monos.getT1();
            TextChannel channel = (TextChannel) monos.getT2();

            // Audit the new message
            List<AuditedEvent> events = this.auditedEvents.computeIfAbsent(channel, x -> new ArrayList<>());
            events.add(new AuditedEvent(channel));

            // Prune old entries
            events.removeIf(AuditedEvent::shouldPrune);
            if (events.isEmpty())
                this.auditedEvents.remove(channel);

            // Are we in danger?
            if (events.size() >= DANGER_THRESHOLD && !this.dangerModes.contains(channel)) {
                this.dangerModes.add(channel);
                channel.edit(spec -> spec.setRateLimitPerUser(MESSAGE_RATE_LIMIT)).subscribe();
                commandManager.sendResponse(Mono.just(channel), "\u26a0 Rate Limited", "This channel has been rate limited due to a sudden influx of messages.\n" +
                        "This rate limit will automatically expire in 10 minutes.").subscribe();

                // Get guild owner and send a DM
                guild.getOwner().flatMap(Member::getPrivateChannel).flatMap(x -> x.createMessage(spec -> {
                    spec.setEmbed(embedSpec -> commandManager.applyEmbedSpec(guild.getId(), embedSpec, "\u26a0 Your guild might be in danger!",
                            "A large influx of messages have been posted in " + channel.getMention() + " in " + guild.getName() + " within the past 10 seconds.\n" +
                                    "We've temporarily put the channel in slowmode for the next 10 minutes as a precaution.", null));
                })).subscribe();

                // Expire automatically after a while
                Mono.delay(Duration.ofMillis(DANGER_UPDATE_THRESHOLD)).subscribe(x -> {
                    channel.edit(spec -> spec.setRateLimitPerUser(0)).subscribe();
                    this.dangerModes.remove(channel);
                    commandManager.sendResponse(Mono.just(channel), "\u26a0 Rate Limited Removed", "The temporary rate limit has been removed from this channel.\n" +
                            "Please avoid spamming the chat.").subscribe();
                });
            }
        });
    }

    private static class AuditedEvent {

        private final TextChannel channel;
        private final long auditTime;

        public AuditedEvent(TextChannel channel) {
            this.channel = channel;
            this.auditTime = System.currentTimeMillis();
        }

        public TextChannel getChannel() {
            return this.channel;
        }

        public boolean shouldPrune() {
            return System.currentTimeMillis() - this.auditTime > PRUNE_TIME;
        }

    }

}
