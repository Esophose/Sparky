package dev.esophose.discordbot.command.commands.misc

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.command.DiscordCommand
import dev.esophose.discordbot.command.DiscordCommandMessage
import dev.esophose.discordbot.manager.CommandManager
import dev.esophose.discordbot.utils.BotUtils
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkGetCommand : DiscordCommand() {

    override val name: String
        get() = "get"

    override val aliases: List<String>
        get() = emptyList()

    override val description: String
        get() = "Executes a GET request for a URL"

    override val requiredBotPermissions: PermissionSet
        get() = PermissionSet.of(Permission.SEND_MESSAGES)

    override val defaultRequiredMemberPermission: Permission
        get() = Permission.ADMINISTRATOR

    fun execute(message: DiscordCommandMessage, url: String) {
        try {
            val request = Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .build()
            val response = OkHttpClient().newBuilder().build().newCall(request).execute()
            val responseCode = response.code
            val responseMessage = response.message
            if (response.isSuccessful) {
                message.channel.cast(TextChannel::class.java).subscribe { BotUtils.sendLargeMessage("Success $responseCode: $responseMessage\n\n" + String(response.body!!.bytes()), it) }
            } else {
                Sparky.getManager(CommandManager::class).sendResponse(message.channel, "Error $responseCode: $responseMessage", "An error recurred executing the given GET request").subscribe()
            }
        } catch (ex: Exception) {
            Sparky.getManager(CommandManager::class).sendResponse(message.channel, "An error occurred during GET", "Message: ${ex.message}").subscribe()
        }
    }

}
