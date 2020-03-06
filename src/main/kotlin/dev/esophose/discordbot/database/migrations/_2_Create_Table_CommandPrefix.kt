package dev.esophose.discordbot.database.migrations

import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.DatabaseConnector
import java.sql.Connection
import java.sql.SQLException

class _2_Create_Table_CommandPrefix : DataMigration(2) {

    @Throws(SQLException::class)
    override fun migrate(connector: DatabaseConnector, connection: Connection) {
        val query = """
|                   CREATE TABLE command_prefix (
|                       guild_id INTEGER NOT NULL PRIMARY KEY,
|                       prefix   TEXT NOT NULL
|                   )
|                   """.trimMargin()
        connection.createStatement().use { it.executeUpdate(query) }
    }

}
