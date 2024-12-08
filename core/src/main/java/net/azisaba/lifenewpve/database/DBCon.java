package net.azisaba.lifenewpve.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("SqlSourceToSinkFlow")
public class DBCon {

    protected static HikariDataSource dataSource;

    protected static String LOOT_CHEST;

    public void initialize(@NotNull LifeNewPvE plugin) {
        if (!plugin.getConfig().getBoolean("Database.use")) return;
        LOOT_CHEST = plugin.getConfig().getString("Database.lootChest");

        setupDataSource(plugin);
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            createTableIfNotExists(statement);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupDataSource(@NotNull LifeNewPvE plugin) {
        HikariConfig config = new HikariConfig();
        String host = plugin.getConfig().getString("Database.host");
        int port = plugin.getConfig().getInt("Database.port");
        String database = plugin.getConfig().getString("Database.database");
        String username = plugin.getConfig().getString("Database.username");
        String password = plugin.getConfig().getString("Database.password");
        String scheme = plugin.getConfig().getString("Database.scheme");

        config.setJdbcUrl(scheme + "://" + host + ":" + port + "/" + database);
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(10);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
    }

    private void createTableIfNotExists(@NotNull Statement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SHOW TABLES LIKE '" + LOOT_CHEST + "'");
        if (!resultSet.next()) {
            statement.executeUpdate("CREATE  TABLE IF NOT EXISTS `" + LOOT_CHEST + "` (" +
                    "`uuid` varchar(36) NOT NULL, " +
                    "`name` varchar(32) NOT NULL, " +
                    "`loc` varchar(128) NOT NULL, " +
                    "`value` tinyint(1) DEFAULT 1, " +
                    "PRIMARY KEY (`uuid`, `name`, `loc`)" +
                    ");");
        }
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
