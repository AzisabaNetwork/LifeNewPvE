package net.azisaba.lifenewpve.database;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@SuppressWarnings("SqlSourceToSinkFlow")
public class DBLootChest extends DBCon {

    public boolean isLooted(@NotNull UUID uuid, @NotNull Location loc) {
        try {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement state = con.prepareStatement("SELECT `value` FROM `" + LOOT_CHEST + "` WHERE `uuid` = ? And `name` = ? And `loc` = ?")) {
                    state.setString(1, uuid.toString());
                    state.setString(2, loc.getWorld().getName());
                    state.setString(3,  "" + loc.getBlockX() + loc.getBlockY() + loc.blockZ());

                    ResultSet resultSet = state.executeQuery();
                    if (resultSet == null) return false;
                    if (resultSet.next()) {
                       return resultSet.getBoolean("value");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void setLooted(@NotNull UUID uuid, @NotNull Location loc, boolean looted) {
        try {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement state = con.prepareStatement("INSERT INTO `" + LOOT_CHEST + "` VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `value` = ?;")) {
                    state.setString(1, uuid.toString());
                    state.setString(2, loc.getWorld().getName());
                    state.setString(3, "" + loc.getBlockX() + loc.getBlockY() + loc.getBlockZ());
                    state.setBoolean(4, looted);
                    state.setBoolean(5, looted);
                    state.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
