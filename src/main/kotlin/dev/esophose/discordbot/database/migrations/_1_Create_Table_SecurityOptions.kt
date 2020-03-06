package dev.esophose.discordbot.database.migrations

import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.DatabaseConnector
import java.sql.Connection
import java.sql.SQLException

class _1_Create_Table_SecurityOptions : DataMigration(1) {

    @Throws(SQLException::class)
    override fun migrate(connector: DatabaseConnector, connection: Connection) {
        val query = """
                    CREATE TABLE security_options (
                        guild_id       INTEGER NOT NULL PRIMARY KEY,
                        member_join    INTEGER NOT NULL,
                        message_create INTEGER NOT NULL
                    )
                    """.trimMargin()
        connection.createStatement().use { it.executeUpdate(query) }
    }

}
