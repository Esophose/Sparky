package dev.esophose.discordbot.database.migrations;

import dev.esophose.discordbot.database.DataMigration;
import dev.esophose.discordbot.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_Create_Table_SecurityOptions extends DataMigration {

    public _1_Create_Table_SecurityOptions() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection) throws SQLException {
        String query =
                "CREATE TABLE security_options (" +
                "    guild_id       INTEGER NOT NULL PRIMARY KEY," +
                "    member_join    INTEGER NOT NULL," +
                "    message_create INTEGER NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

}
