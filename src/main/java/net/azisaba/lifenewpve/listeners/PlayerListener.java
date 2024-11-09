package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.commands.WorldTeleportCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerListener implements Listener {
    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener.Command(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Interact(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Quit(), lifeNewPvE);
    }

    public static class Quit extends PlayerListener {

        @EventHandler
        public void onQuit(@NotNull PlayerQuitEvent e) {
            WorldTeleportCommand.clearTeleporting(e.getPlayer());
        }
    }

    public static class ChangeWorld extends WorldListener {

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

    public static class Interact extends PlayerListener {

        @EventHandler
        public void onInteract(@NotNull PlayerInteractEvent e) {
            if (!e.getAction().equals(Action.PHYSICAL)) return;
            Block b = e.getClickedBlock();
            if (b == null) return;
            Player p = e.getPlayer();

            Vector v;
            if (b.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                v = p.getEyeLocation().getDirection().clone().normalize().multiply(3).setY(4);
            } else if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                v = p.getEyeLocation().getDirection().clone().normalize().multiply(4).setY(3);
            } else return;

            p.setVelocity(v);
            p.playSound(p, Sound.BLOCK_PISTON_EXTEND, 1, 1);
            AtomicBoolean cancel = new AtomicBoolean(false);
            for (int i = 1; i <= 120; i++) {
                if (cancel.get()) break;
                JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> {

                    if (p.isOnGround()) {
                        cancel.set(true);
                        return;
                    }
                    Vector vector = p.getVelocity();
                    double y = vector.getY();
                    p.setVelocity(vector.clone().add(p.getEyeLocation().getDirection().clone().multiply(0.1)).multiply(1.05).setY(y));
                    p.setFallDistance(0);
                }, i);
            }
        }
    }
}
