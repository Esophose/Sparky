package dev.esophose.discordbot.database.migrations;

import dev.esophose.discordbot.database.DataMigration;
import dev.esophose.discordbot.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_Create_Table_EmbedColor extends DataMigration {

    public _3_Create_Table_EmbedColor() {
        super(3);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection) throws SQLException {
        String query =
                "CREATE TABLE embed_color (" +
                "    guild_id INTEGER NOT NULL PRIMARY KEY," +
                "    color    TEXT NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

}
