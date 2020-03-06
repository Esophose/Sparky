package dev.esophose.discordbot.database.migrations

import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.DatabaseConnector
import java.sql.Connection
import java.sql.SQLException

class _4_Create_Table_AutoRole : DataMigration(4) {

    @Throws(SQLException::class)
    override fun migrate(connector: DatabaseConnector, connection: Connection) {
        val query = """
                    CREATE TABLE auto_role (
                        guild_id INTEGER NOT NULL,
                        role_id  INTEGER NOT NULL
                    )
                    """.trimIndent()
        connection.createStatement().use { it.executeUpdate(query) }
    }

}
