package dev.esophose.discordbot.manager

import dev.esophose.discordbot.Sparky
import dev.esophose.discordbot.database.DataMigration
import dev.esophose.discordbot.database.SQLiteConnector
import dev.esophose.discordbot.database.migrations._0_Create_Table_CommandPermissionOverride
import dev.esophose.discordbot.database.migrations._1_Create_Table_SecurityOptions
import dev.esophose.discordbot.database.migrations._2_Create_Table_CommandPrefix
import dev.esophose.discordbot.database.migrations._3_Create_Table_EmbedColor
import dev.esophose.discordbot.database.migrations._4_Create_Table_AutoRole
import dev.esophose.discordbot.database.migrations._5_Create_Table_MessageAudit
import java.util.Comparator
import kotlin.streams.toList

class DataMigrationManager : Manager() {

    private val migrations: List<DataMigration> = listOf(
            _0_Create_Table_CommandPermissionOverride(),
            _1_Create_Table_SecurityOptions(),
            _2_Create_Table_CommandPrefix(),
            _3_Create_Table_EmbedColor(),
            _4_Create_Table_AutoRole(),
            _5_Create_Table_MessageAudit()
    )

    override fun enable() {
        val databaseConnector = Sparky.connector

        databaseConnector.connect { connection ->
            var currentMigration = -1

            val query: String = if (databaseConnector is SQLiteConnector) {
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?"
            } else {
                "SHOW TABLES LIKE ?"
            }

            var migrationsExist: Boolean
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, TABLE_NAME)
                migrationsExist = statement.executeQuery().next()
            }

            if (!migrationsExist) {
                // No migration table exists, create one
                val createTable = "CREATE TABLE $TABLE_NAME (migration_version INTEGER NOT NULL)"
                connection.prepareStatement(createTable).use { statement -> statement.execute() }

                // Insert primary row into migration table
                val insertRow = "INSERT INTO $TABLE_NAME VALUES (?)"
                connection.prepareStatement(insertRow).use { statement ->
                    statement.setInt(1, -1)
                    statement.execute()
                }
            } else {
                // Grab the current migration version
                val selectVersion = "SELECT migration_version FROM $TABLE_NAME"
                connection.prepareStatement(selectVersion).use { statement ->
                    val result = statement.executeQuery()
                    result.next()
                    currentMigration = result.getInt("migration_version")
                }
            }

            // Grab required migrations
            val finalCurrentMigration = currentMigration
            val requiredMigrations = this.migrations
                    .stream()
                    .filter { x -> x.revision > finalCurrentMigration }
                    .sorted(Comparator.comparingInt { it.revision })
                    .toList()

            if (requiredMigrations.isNotEmpty()) {
                // Migrate the data
                for (dataMigration in requiredMigrations) {
                    dataMigration.migrate(databaseConnector, connection)
                    println("Applied data migration " + dataMigration.javaClass.simpleName)
                }

                // Set the new current migration to be the highest migrated to
                currentMigration = requiredMigrations
                        .stream()
                        .mapToInt { it.revision }
                        .max()
                        .orElse(-1)

                val updateVersion = "UPDATE $TABLE_NAME SET migration_version = ?"
                connection.prepareStatement(updateVersion).use { statement ->
                    statement.setInt(1, currentMigration)
                    statement.execute()
                }
            }
        }
    }

    companion object {
        private const val TABLE_NAME = "migrations"
    }

}
