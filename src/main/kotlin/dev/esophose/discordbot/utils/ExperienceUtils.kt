package dev.esophose.discordbot.utils

import discord4j.common.util.Snowflake
import java.util.Random
import kotlin.math.ceil

/**
 * An implementation of how the Minecraft experience system works
 * Formulas found here: https://minecraft.gamepedia.com/Experience#Values_from_Java_Edition_1.3.1.E2.80.931.8_.2814w02a.29
 */
object ExperienceUtils {

    fun getXPForUser(userId: Snowflake, messageCount: Int): Int {
        val random = Random(userId.asLong())
        var xp = 0
        for (i in 0 until messageCount)
            xp += random.nextInt(3) + 1
        return xp
    }

    fun getXPRequiredForLevel(level: Int): Int {
        return when {
            level < 15 -> 17 * level
            level < 30 -> ceil(1.5 * level * level - 29.5 * level + 360).toInt()
            else -> ceil(3.5 * level * level - 151.5 * level + 2220).toInt()
        }
    }

    fun getCurrentLevel(xp: Int): Int {
        var level = 0
        while (getXPRequiredForLevel(level) <= xp)
            level++
        return level
    }

    fun getXPToNextLevel(xp: Int): Int {
        val currentLevel = getCurrentLevel(xp)
        return getXPRequiredForLevel(currentLevel + 1) - xp
    }

}