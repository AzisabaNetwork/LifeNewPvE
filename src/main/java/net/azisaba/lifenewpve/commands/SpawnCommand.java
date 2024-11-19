package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final LifeNewPvE plugin;

    public SpawnCommand(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        World w = Bukkit.getWorld("world");
        if (w == null) return false;
        Location loc = plugin.getConfig().getLocation("Spawn", new Location(w, 0.5, 64, 0.5, 0, 0));

        if (args.length == 1) {
            String name = args[0];
            Player p = Bukkit.getPlayer(name);
            if (p != null) {
                if (p.teleport(loc)) {
                    p.sendMessage(Component.text("§a§lスポーンにテレポートしました！"));
                    return true;
                }
                p.sendMessage(Component.text("§c§lテレポートに失敗しました。"));
                return true;
            }
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§c§lプレイヤーでないため、スポーンにテレポートできません。"));
            return false;
        }
        if (p.getWorld().getName().equals("world")) return false;
        if (p.teleport(loc)) {
            p.sendMessage(Component.text("§a§lスポーンにテレポートしました！"));
            return true;
        }
        p.sendMessage(Component.text("§c§lテレポートに失敗しました。"));
        return false;
    }
}
