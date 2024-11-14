package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.commands.WorldTeleportCommand;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener.Command(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Quit(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.ChangeWorld(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Interact(), lifeNewPvE);
    }

    public static class Interact extends PlayerListener implements VectorTask {

        @EventHandler
        public void onInteract(@NotNull PlayerInteractEvent e) {
            if (!e.getAction().equals(Action.PHYSICAL)) return;
            Block block = e.getClickedBlock();
            if (block == null) return;
            Player player = e.getPlayer();
            applyVelocityAndRunTask(player, player, getVector(block, player));

        }
    }

    public static class Quit extends PlayerListener {

        @EventHandler
        public void onQuit(@NotNull PlayerQuitEvent e) {
            WorldTeleportCommand.clearTeleporting(e.getPlayer());
        }
    }

    public static class ChangeWorld extends PlayerListener {

        @EventHandler
        public void onWorldChange(@NotNull PlayerChangedWorldEvent e) {
            Player p = e.getPlayer();
            if (MultiverseListener.isResetWorld(p.getWorld().getName())) {
                p.teleport(e.getFrom().getSpawnLocation());
            }
        }
    }

    public static class Command extends PlayerListener {

        @EventHandler(ignoreCancelled = true)
        public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
            Player p = e.getPlayer();
            if (!p.hasPermission("lifenewpve.reload.mythicmobs")) return;
            if (e.getMessage().contains("mm r") || e.getMessage().contains("mythicmobs r")) {
                MythicListener.call();
            }
        }
    }
}
