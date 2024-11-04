package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorldRegenCommand implements TabExecutor {

    private final LifeNewPvE plugin;

    public WorldRegenCommand(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return false;
        if (args.length != 1) {
            p.sendMessage("§e/worldregen <ワールド名> を使用してください。");
            return false;
        }
        String name = args[0];
        World w;
        try {
            w = Bukkit.getWorld(name);
            if (w == null) {
                p.sendMessage("§cそのワールドは存在しないです。");
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        World spawn = Bukkit.getWorld("world");
        if (spawn == null) {
            p.sendMessage("§cworldは必ず生成してください。");
            return true;
        }
        if (plugin.getConfig()
                .getLocation("Spawn", new Location(spawn, 0.5, 64, 0.5))
                .getWorld().getName().equals(name)) {
            p.sendMessage("§cスポーンワールドは再生成できません。");
            return true;
        }

        List<Player> players = w.getPlayers();
        players.forEach(player -> {
            if (!player.getPassengers().isEmpty()) {
                player.setSneaking(true);
            }
        });
        players.forEach(player -> {
            player.sendMessage("§aワールドリジェネレーションが開始されました。");
            player.performCommand("spawn");
        });
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            String name = args[0];
            Bukkit.getWorlds().forEach(world -> {
                if (!world.getName().toLowerCase().contains(name.toLowerCase())) return;
                list.add(world.getName());
            });
            return list;
        }
        return null;
    }
}
