package dev.esophose.discordbot.manager;

import dev.esophose.discordbot.Sparky;
import dev.esophose.discordbot.database.DataMigration;
import dev.esophose.discordbot.database.DatabaseConnector;
import dev.esophose.discordbot.database.SQLiteConnector;
import dev.esophose.discordbot.database.migrations._0_Create_Table_CommandPermissionOverride;
import dev.esophose.discordbot.database.migrations._1_Create_Table_SecurityOptions;
import dev.esophose.discordbot.database.migrations._2_Create_Table_CommandPrefix;
import dev.esophose.discordbot.database.migrations._3_Create_Table_EmbedColor;
import dev.esophose.discordbot.database.migrations._4_Create_Table_AutoRole;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataMigrationManager extends Manager {

    private static final String TABLE_NAME = "migrations";

    private List<DataMigration> migrations;

    public DataMigrationManager(Sparky bot) {
        super(bot);

        this.migrations = Arrays.asList(
                new _0_Create_Table_CommandPermissionOverride(),
                new _1_Create_Table_SecurityOptions(),
                new _2_Create_Table_CommandPrefix(),
                new _3_Create_Table_EmbedColor(),
                new _4_Create_Table_AutoRole()
        );
    }

    @Override
    public void enable() {
        DatabaseConnector databaseConnector = this.bot.getConnector();

        databaseConnector.connect((connection -> {
            int currentMigration = -1;
            boolean migrationsExist;

            String query;
            if (databaseConnector instanceof SQLiteConnector) {
                query = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
            } else {
                query = "SHOW TABLES LIKE ?";
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, TABLE_NAME);
                migrationsExist = statement.executeQuery().next();
            }

            if (!migrationsExist) {
                // No migration table exists, create one
                String createTable = "CREATE TABLE " + TABLE_NAME + " (migration_version INTEGER NOT NULL)";
                try (PreparedStatement statement = connection.prepareStatement(createTable)) {
                    statement.execute();
                }

                // Insert primary row into migration table
                String insertRow = "INSERT INTO " + TABLE_NAME + " VALUES (?)";
                try (PreparedStatement statement = connection.prepareStatement(insertRow)) {
                    statement.setInt(1, -1);
                    statement.execute();
                }
            } else {
                // Grab the current migration version
                String selectVersion = "SELECT migration_version FROM " + TABLE_NAME;
                try (PreparedStatement statement = connection.prepareStatement(selectVersion)) {
                    ResultSet result = statement.executeQuery();
                    result.next();
                    currentMigration = result.getInt("migration_version");
                }
            }

            // Grab required migrations
            int finalCurrentMigration = currentMigration;
            List<DataMigration> requiredMigrations = this.migrations
                    .stream()
                    .filter(x -> x.getRevision() > finalCurrentMigration)
                    .sorted(Comparator.comparingInt(DataMigration::getRevision))
                    .collect(Collectors.toList());

            // Nothing to migrate, abort
            if (requiredMigrations.isEmpty())
                return;

            // Migrate the data
            for (DataMigration dataMigration : requiredMigrations) {
                dataMigration.migrate(databaseConnector, connection);
                System.out.println("Applied data migration " + dataMigration.getClass().getSimpleName());
            }

            // Set the new current migration to be the highest migrated to
            currentMigration = requiredMigrations
                    .stream()
                    .mapToInt(DataMigration::getRevision)
                    .max()
                    .orElse(-1);

            String updateVersion = "UPDATE " + TABLE_NAME + " SET migration_version = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateVersion)) {
                statement.setInt(1, currentMigration);
                statement.execute();
            }
        }));
    }

}
