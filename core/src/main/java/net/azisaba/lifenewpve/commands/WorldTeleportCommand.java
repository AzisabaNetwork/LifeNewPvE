package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.listeners.MultiverseListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WorldTeleportCommand implements TabExecutor {

    private static final Set<UUID> TELEPORT_PLAYER = new HashSet<>();

    private static final String WORLD_NOT_FOUND_MSG = "§cそのワールドは存在しません。";
    private static final String WORLD_RESETTING_MSG = "§cそのワールドはリセット中です。";
    private static final String PLAYER_NOT_FOUND_MSG = "§cそのプレイヤーは見つかりません。";
    private static final String INVALID_LOCATION_MSG = "その場所は無効です。";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return handlePlayerCommand(player, args);
        } else if (args.length == 2) {
            return handleOtherSender(sender, args, false);
        } else if (args.length == 3) {
            return handleOtherSender(sender, args, true);
        }
        return false;
    }

    private boolean handlePlayerCommand(Player player, @NotNull String[] args) {
        String worldName;
        if (args.length < 1) {
            worldName = player.getWorld().getName();
            player.teleport(player.getWorld().getSpawnLocation());
        } else {
            World world = Bukkit.getWorld(args[0]);
            if (!isValidWorld(world, player)) return false;
            assert world != null;
            worldName = world.getName();
            player.teleport(world.getSpawnLocation());
            if (worldName.contains("resource")) {
                TELEPORT_PLAYER.add(player.getUniqueId());
            }
        }
        player.sendMessage(Component.text("§a§l" + worldName + "のスポーンにテレポートしました！"));
        return true;
    }

    private boolean handleOtherSender(CommandSender sender, @NotNull String[] args, boolean hasLocation) {
        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        if (!isValidWorld(world, sender)) return false;
        assert world != null;

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(PLAYER_NOT_FOUND_MSG);
            return false;
        }

        if (hasLocation) {
            String[] coords = args[2].split(",");
            Location location = createLocation(world, coords, sender);
            if (location == null) return false;
            player.teleport(location);
        } else {
            player.teleport(world.getSpawnLocation());
        }

        player.sendMessage("§a§l" + worldName + "ワールドにテレポートしました。");
        if (worldName.contains("resource")) {
            TELEPORT_PLAYER.add(player.getUniqueId());
        }
        return true;
    }

    private boolean isValidWorld(World world, CommandSender sender) {
        if (world == null) {
            sender.sendMessage(WORLD_NOT_FOUND_MSG);
            return false;
        }
        if (MultiverseListener.isResetWorld(world.getName())) {
            sender.sendMessage(WORLD_RESETTING_MSG);
            return false;
        }
        return true;
    }

    @Nullable
    private Location createLocation(World world, @NotNull String[] coords, CommandSender sender) {
        try {
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            double z = Double.parseDouble(coords[2]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            sender.sendMessage(INVALID_LOCATION_MSG);
            return null;
        }
    }

    public static boolean isTeleporting(@NotNull Player p) {
        return TELEPORT_PLAYER.contains(p.getUniqueId());
    }

    public static void clearTeleporting(@NotNull Player p) {
        TELEPORT_PLAYER.remove(p.getUniqueId());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return null;
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            Bukkit.getWorlds().forEach(w -> {
                if (w.getName().equals(p.getWorld().getName())) return;
                list.add(w.getName());
            });
            return list;
        }
        return null;
    }
}
