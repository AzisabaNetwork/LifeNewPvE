package net.azisaba.lifenewpve.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModeCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return false;
        if (args.length < 1) {
            switchMode(p);
        } else if (args[0].equalsIgnoreCase("on")) {
            switchMode(p, true);
        } else if (args[0].equalsIgnoreCase("off")) {
            switchMode(p, false);
        } else {
            switchMode(p);
        }
        return true;
    }

    public static void switchMode(@NotNull Player p, boolean bypass) {
        String mode = bypass ? "enable" : "disable";
        p.performCommand("egod " + mode);
        p.performCommand("efly " + mode);
        String m = (bypass) ? "on" : "off";
        p.performCommand("rg bypass " + m);
        p.sendMessage(Component.text("§f§l運営モードを §b§l" + bypass + " §f§lに切り替えました。"));
    }

    public static void switchMode(@NotNull Player p) {
        p.performCommand("egod " + p.getName());
        p.performCommand("efly " + p.getName());
        p.performCommand("rg bypass");
        p.sendMessage(Component.text("§f§l運営モードを切り替えました。"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }
        return List.of();
    }
}
