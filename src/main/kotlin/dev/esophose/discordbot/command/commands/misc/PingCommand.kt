package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import discord4j.gateway.GatewayClient
import java.io.IOException
import java.net.InetAddress
import java.time.Duration
import java.util.Collections
import java.util.Optional
import java.util.concurrent.TimeUnit
import reactor.core.publisher.Mono

class PingCommand : DiscordCommand() {

    override val name: String
        get() = "ping"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Displays the ping to Discord or a hostname"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.SEND_MESSAGES

    fun execute(message: DiscordCommandMessage, targetAddress: Optional<InetAddress>) {
        val commandManager = Sparky.getManager(CommandManager::class)

        if (targetAddress.isEmpty) {
            val gatewayClient = Sparky.discord.getGatewayClient(0)
            val responseTime = TimeUnit.MILLISECONDS.convert(gatewayClient.map { it.responseTime }.orElse(Duration.ZERO))
            commandManager.sendResponse(message.channel, "Pong!", "Response time: " + responseTime + "ms").subscribe()
            return
        }

        val address = targetAddress.get()
        commandManager.sendResponse(message.channel, "Pinging " + address.hostAddress, "Please wait...").subscribe { pingMessage ->
            Mono.fromRunnable<Any> {
                val start = System.currentTimeMillis()
                val response = try {
                    if (address.isReachable(3000)) {
                        val end = System.currentTimeMillis() - start
                        "Response received in " + end + "ms"
                    } else {
                        "Unable to reach host address after 3000ms"
                    }
                } catch (e: IOException) {
                    "An error occurred trying to reach the host address"
                }

                val hostAddress: String = if (address.hostName.equals("localhost", ignoreCase = true) || address.hostAddress == "127.0.0.1") {
                    "localhost (127.0.0.1)" // Don't expose our IP address
                } else if (address.hostName == address.hostAddress) {
                    address.hostAddress
                } else {
                    address.hostName + " (" + address.hostAddress + ")"
                }

                pingMessage.edit { messageSpec -> messageSpec.setEmbed { embedSpec -> commandManager.applyEmbedSpec(message.guildId, embedSpec, "Pinged $hostAddress", response, null) } }.subscribe()
            }.subscribe()
        }
    }

}
