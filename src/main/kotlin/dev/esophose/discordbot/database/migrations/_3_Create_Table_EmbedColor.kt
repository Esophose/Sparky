package dev.esophose.discordbot.database.migrations

import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.DatabaseConnector
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class _3_Create_Table_EmbedColor : DataMigration(3) {

    @Throws(SQLException::class)
    override fun migrate(connector: DatabaseConnector, connection: Connection) {
        val query = """
                    CREATE TABLE embed_color (
                        guild_id INTEGER NOT NULL PRIMARY KEY,
                        color    TEXT NOT NULL
                    )
                    """.trimIndent()
        connection.createStatement().use { it.executeUpdate(query) }
    }

}
