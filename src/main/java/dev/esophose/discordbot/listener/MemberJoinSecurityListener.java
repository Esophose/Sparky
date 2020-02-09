package dev.esophose.discordbot.listener;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;

public class MemberJoinSecurityListener extends Listener<MemberJoinEvent> {

    private final static int DANGER_THRESHOLD = 10; // 10 joins
    private final static int PRUNE_TIME = 1000 * 30; // 30 seconds
    private final static int DANGER_UPDATE_THRESHOLD = 1000 * 60 * 3; // 3 minutes

    private final Map<Guild, List<AuditedEvent>> auditedEvents;
    private final Map<Guild, Long> dangerModes;

    public MemberJoinSecurityListener() {
        super(MemberJoinEvent.class);

        this.auditedEvents = Collections.synchronizedMap(new HashMap<>());
        this.dangerModes = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void execute(MemberJoinEvent event) {
        event.getGuild().subscribe(guild -> {
            Member member = event.getMember();
            CommandManager commandManager = Sparky.getInstance().getManager(CommandManager.class);

            // If we're in danger, check if we should continue
            if (this.dangerModes.containsKey(guild)) {
                long lastDangerUpdate = this.dangerModes.get(guild);
                if (System.currentTimeMillis() - lastDangerUpdate > DANGER_UPDATE_THRESHOLD) {
                    this.dangerModes.remove(guild);
                } else {
                    member.kick().subscribe();
                    this.dangerModes.put(guild, System.currentTimeMillis());
                    return;
                }
            }

            // Audit the new member joining
            List<AuditedEvent> events = this.auditedEvents.computeIfAbsent(guild, x -> new ArrayList<>());
            events.add(new AuditedEvent(member));

            // Prune old entries
            events.removeIf(AuditedEvent::shouldPrune);
            if (events.isEmpty())
                this.auditedEvents.remove(guild);

            // Are we in danger?
            if (events.size() >= DANGER_THRESHOLD && !this.dangerModes.containsKey(guild)) {
                this.dangerModes.put(guild, System.currentTimeMillis());
                Flux.fromIterable(events).map(AuditedEvent::getMember).flatMap(Member::kick).subscribe();
                this.auditedEvents.remove(guild);
                // Get guild owner and send a DM
                guild.getOwner().flatMap(Member::getPrivateChannel).flatMap(x -> x.createMessage(spec -> {
                    spec.setEmbed(embedSpec -> commandManager.applyEmbedSpec(guild.getId(), embedSpec, "\u26a0 Your guild might be in danger!",
                            "A large influx of users have joined " + guild.getName() + " within the past minute.\n" +
                                    "We've temporarily disabled allowing members to join for the next 3 minutes or as long as this potential danger continues.", null));
                })).subscribe();
            }
        });
    }

    private static class AuditedEvent {

        private final Member member;
        private final long auditTime;

        public AuditedEvent(Member member) {
            this.member = member;
            this.auditTime = System.currentTimeMillis();
        }

        public Member getMember() {
            return this.member;
        }

        public boolean shouldPrune() {
            return System.currentTimeMillis() - this.auditTime > PRUNE_TIME;
        }

    }

}
