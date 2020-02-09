package dev.esophose.discordbot.command.commands.misc;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.command.DiscordCommand;
import dev.esophose.discordbot.command.DiscordCommandMessage;
import dev.esophose.discordbot.manager.CommandManager;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.gateway.GatewayClient;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;

public class PingCommand extends DiscordCommand {

    public PingCommand(Sparky bot) {
        super(bot);
    }

    public void execute(DiscordCommandMessage message, Optional<InetAddress> targetAddress) {
        CommandManager commandManager = this.bot.getManager(CommandManager.class);

        if (targetAddress.isEmpty()) {
            Optional<GatewayClient> gatewayClient = this.bot.getDiscord().getGatewayClient(0);
            long responseTime = TimeUnit.MILLISECONDS.convert(gatewayClient.map(GatewayClient::getResponseTime).orElse(Duration.ZERO));
            commandManager.sendResponse(message.getChannel(), "Pong!", "Response time: " + responseTime + "ms").subscribe();
            return;
        }

        InetAddress address = targetAddress.get();
        commandManager.sendResponse(message.getChannel(), "Pinging " + address.getHostAddress(), "Please wait...").subscribe(pingMessage -> {
            Mono.fromRunnable(() -> {
                long start = System.currentTimeMillis();
                String response;
                try {
                    if (address.isReachable(3000)) {
                        long end = System.currentTimeMillis() - start;
                        response = "Response received in " + end + "ms";
                    } else {
                        response = "Unable to reach host address after 3000ms";
                    }
                } catch (IOException e) {
                    response = "An error occurred trying to reach the host address";
                }

                String hostAddress;
                if (address.getHostName().equalsIgnoreCase("localhost") || address.getHostAddress().equals("127.0.0.1")) {
                    hostAddress = "localhost (127.0.0.1)"; // Don't expose our IP address
                } else if (address.getHostName().equals(address.getHostAddress())) {
                    hostAddress = address.getHostAddress();
                } else {
                    hostAddress = address.getHostName() + " (" + address.getHostAddress() + ")";
                }

                final String fResponse = response;
                pingMessage.edit(messageSpec -> messageSpec.setEmbed(embedSpec ->
                        commandManager.applyEmbedSpec(message.getGuildId(), embedSpec, "Pinged " + hostAddress, fResponse, null))).subscribe();
            }).subscribe();
        });
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Displays the ping to Discord or a hostname";
    }

    @Override
    public PermissionSet getRequiredBotPermissions() {
        return PermissionSet.of(Permission.SEND_MESSAGES);
    }

    @Override
    public Permission getDefaultRequiredMemberPermission() {
        return Permission.SEND_MESSAGES;
    }

}
