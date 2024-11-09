package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.listeners.MultiverseListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p) {
            String name;
            if (args.length < 1) {
                name = p.getWorld().getName();
                p.teleport(p.getWorld().getSpawnLocation());
            } else {
                World w = Bukkit.getWorld(args[0]);
                if (w == null) {
                    p.sendMessage(Component.text("§c§lそのワールドは存在しません。"));
                    return false;
                }
                name = w.getName();
                if (MultiverseListener.isResetWorld(name)) {
                    sender.sendMessage("§cそのワールドはリセット中です。");
                    return false;
                }
                p.teleport(w.getSpawnLocation());
                if (name.contains("resource")) {
                    TELEPORT_PLAYER.add(p.getUniqueId());
                }
            }
            p.sendMessage(Component.text("§a§l" + name + "のスポーンにテレポートしました！"));
            return true;
        } else if (args.length == 2) {
            String name = args[0];
            World w = Bukkit.getWorld(name);
            if (w == null) {
                sender.sendMessage("§cそのワールドは存在しないです。");
                return false;
            } else {
                if (MultiverseListener.isResetWorld(name)) {
                    sender.sendMessage("§cそのワールドはリセット中です。");
                    return false;
                }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage("§cそのプレイヤーは見つかりません。");
                    return false;
                }
                p.teleport(w.getSpawnLocation());
                p.sendMessage("§a§l" + name + "ワールドにテレポートしました。");
                if (name.contains("resource")) {
                    TELEPORT_PLAYER.add(p.getUniqueId());
                }
                return true;
            }
        } else return false;
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
