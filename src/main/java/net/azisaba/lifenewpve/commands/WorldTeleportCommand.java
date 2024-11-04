package net.azisaba.lifenewpve.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorldTeleportCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return false;
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
            p.teleport(w.getSpawnLocation());
        }
        p.sendMessage(Component.text("§a§l" + name + "のスポーンにテレポートしました！"));
        return true;
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
