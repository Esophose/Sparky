package dev.esophose.discordbot.database.migrations

import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.DatabaseConnector
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class _0_Create_Table_CommandPermissionOverride : DataMigration(0) {

    @Throws(SQLException::class)
    override fun migrate(connector: DatabaseConnector, connection: Connection) {
        val query = "CREATE TABLE command_permission_override (" +
                    "    guild_id            INTEGER NOT NULL," +
                    "    command_name        TEXT NOT NULL," +
                    "    required_permission TEXT NOT NULL," +
                    "    PRIMARY KEY(guild_id, command_name)" +
                    ")"
        connection.createStatement().use { statement -> statement.executeUpdate(query) }
    }

}
