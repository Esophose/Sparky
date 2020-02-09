package dev.esophose.discordbot.command.arguments;

import dev.esophose.discordbot.command.DiscordCommandArgumentHandler;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;

public class RoleArgumentHandler extends DiscordCommandArgumentHandler<Role> {

    @Override
    protected Mono<Role> handleInternal(Guild guild, String input) {
        return new SnowflakeArgumentHandler()
                .handleInternal(guild, input)
                .flatMap(guild::getRoleById)
                .switchIfEmpty(Mono.from(guild.getRoles().filter(x -> this.matchesRole(input, x))));
    }

    private boolean matchesRole(String input, Role role) {
        String name = role.getName();
        String mention = role.getMention();

        return input.equalsIgnoreCase(name)
            || input.equalsIgnoreCase(mention);
    }

    @Override
    public String getErrorMessage(Guild guild, String input) {
        return "Invalid role: [" + input + "]";
    }

    @Override
    public Class<Role> getHandledType() {
        return Role.class;
    }

}
