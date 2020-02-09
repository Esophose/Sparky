package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public class MemberArgumentHandler extends DiscordCommandArgumentHandler<Member> {

    @Override
    protected Mono<Member> handleInternal(Guild guild, String input) {
        return new SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap(guild::getMemberById)
                .switchIfEmpty(Mono.from(guild.getMembers().filter(x -> this.matchesUsername(input, x))));
    }

    private boolean matchesUsername(String input, Member member) {
        String username = member.getUsername();
        String discriminator = member.getDiscriminator();
        String full = username + '#' + discriminator;
        String mention = member.getMention();
        String nicknameMention = member.getNicknameMention();
        String displayName = member.getDisplayName();

        return input.equalsIgnoreCase(username)
            || input.equalsIgnoreCase(full)
            || input.equalsIgnoreCase(displayName)
            || input.equals(mention)
            || input.equals(nicknameMention);
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid member: [" + input + "]";
    }

    @Override
    public Class<Member> getHandledType() {
        return Member.class;
    }

}
