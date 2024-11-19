package net.azisaba.lifenewpve.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.session.Session;
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

    public static void switchMode(Player p, boolean bypass) {
        Session session = getSession(p);
        if (session == null) return;

        session.setBypassDisabled(bypass);
        String mode = bypass ? "enable" : "disable";
        p.performCommand("egod " + p.getName() + " " + mode);
        p.performCommand("efly " + p.getName() + " " + mode);
        p.sendMessage(Component.text("§f§l運営モードを §b§l" + bypass + " §f§lに切り替えました。"));
    }

    public static void switchMode(Player p) {
        Session session = getSession(p);
        if (session == null) return;

        boolean disabled = !session.hasBypassDisabled();
        String mode = disabled ? "enable" : "disable";
        p.performCommand("egod " + p.getName() + " " + mode);
        p.performCommand("efly " + p.getName() + " " + mode);
        session.setBypassDisabled(disabled);
        p.sendMessage(Component.text("§f§l運営モードを §b§l" + disabled + " §f§lに切り替えました。"));
    }

    @Nullable
    private static Session getSession(Player p) {
        LocalPlayer player = new BukkitPlayer(WorldGuardPlugin.inst(), p);
        return WorldGuard.getInstance().getPlatform().getSessionManager().getIfPresent(player);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }
        return List.of();
    }
}
