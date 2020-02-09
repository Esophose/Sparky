package dev.esophose.discordbot.database.migrations;

import dev.esophose.discordbot.database.DataMigration;
import dev.esophose.discordbot.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_Create_Table_CommandPrefix extends DataMigration {

    public _2_Create_Table_CommandPrefix() {
        super(2);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection) throws SQLException {
        String query =
                "CREATE TABLE command_prefix (" +
                "    guild_id INTEGER NOT NULL PRIMARY KEY," +
                "    prefix   TEXT NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

}
