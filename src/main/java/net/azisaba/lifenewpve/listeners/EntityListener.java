package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class EntityListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntityListener.Interact(), lifeNewPvE);
    }

    public static class Interact extends EntityListener {

        @EventHandler
        public void onEntityInteract(@NotNull EntityInteractEvent e) {
            if (e.getEntity() instanceof Player) return;
            if (!(e.getEntity() instanceof LivingEntity living) || living.getPassengers().isEmpty()) return;
            for (Entity entity : living.getPassengers()) {
                if (entity instanceof Player p) {
                    Block b = e.getBlock();
                    Vector v;
                    if (b.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                        v = p.getEyeLocation().getDirection().clone().normalize().multiply(3).setY(4);
                    } else if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                        v = p.getEyeLocation().getDirection().clone().normalize().multiply(4).setY(3);
                    } else return;

                    living.setVelocity(v);
                    p.playSound(p, Sound.BLOCK_PISTON_EXTEND, 1, 1);
                    AtomicBoolean cancel = new AtomicBoolean(false);
                    for (int i = 1; i <= 120; i++) {
                        if (cancel.get()) break;
                        JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> {

                            if (living.isOnGround()) {
                                cancel.set(true);
                                return;
                            }
                            Vector vector = living.getVelocity();
                            double y = vector.getY();
                            living.setVelocity(vector.clone().add(p.getEyeLocation().getDirection().clone().multiply(0.1)).multiply(1.05).setY(y));
                            living.setFallDistance(0);
                            p.setFallDistance(0);
                        }, i);
                    }
                    return;
                }
            }
        }
    }
}
