package dev.esophose.discordbot.database.migrations;

import dev.esophose.discordbot.database.DataMigration;
import dev.esophose.discordbot.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _0_Create_Table_CommandPermissionOverride extends DataMigration {

    public _0_Create_Table_CommandPermissionOverride() {
        super(0);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection) throws SQLException {
        String query =
                "CREATE TABLE command_permission_override (" +
                "    guild_id            INTEGER NOT NULL," +
                "    command_name        TEXT NOT NULL," +
                "    required_permission TEXT NOT NULL," +
                "    PRIMARY KEY(guild_id, command_name)" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

}
