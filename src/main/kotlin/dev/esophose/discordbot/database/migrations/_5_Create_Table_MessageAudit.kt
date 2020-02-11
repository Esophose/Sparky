package dev.esophose.discordbot.database.migrations

import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.DatabaseConnector
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class _5_Create_Table_MessageAudit : DataMigration(5) {

    @Throws(SQLException::class)
    override fun migrate(connector: DatabaseConnector, connection: Connection) {
        val query = """
                    CREATE TABLE message_audit (
                        guild_id   INTEGER NOT NULL,
                        channel_id INTEGER NOT NULL,
                        author_id  INTEGER NOT NULL,
                        message_id INTEGER NOT NULL,
                        datetime   TEXT NOT NULL,
                        content    TEXT NOT NULL,
                        type       TEXT NOT NULL
                    )
                    """.trimIndent()
        connection.createStatement().use { it.executeUpdate(query) }

        val index = "CREATE INDEX message_audit_index ON message_audit (type, guild_id, author_id, message_id)"
        connection.createStatement().use { it.executeUpdate(index) }
    }

}
