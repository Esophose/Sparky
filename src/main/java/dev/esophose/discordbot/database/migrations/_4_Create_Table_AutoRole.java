package dev.esophose.discordbot.database.migrations;

import dev.esophose.discordbot.database.DataMigration;
import dev.esophose.discordbot.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _4_Create_Table_AutoRole extends DataMigration {

    public _4_Create_Table_AutoRole() {
        super(4);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection) throws SQLException {
        String query =
                "CREATE TABLE auto_role (" +
                "    guild_id INTEGER NOT NULL," +
                "    role_id  INTEGER NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

}
