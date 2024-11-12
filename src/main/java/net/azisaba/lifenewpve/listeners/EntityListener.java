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


        public Vector getVector(@NotNull Block b, LivingEntity p) {
            if (b.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                return p.getEyeLocation().getDirection().clone().normalize().multiply(3).setY(4);
            } else if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                return p.getEyeLocation().getDirection().clone().normalize().multiply(4).setY(3);
            }
            return new Vector(0, 0, 0);
        }


        @EventHandler
        public void onEntityInteract(@NotNull EntityInteractEvent event) {
            if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

            for (Entity passenger : livingEntity.getPassengers()) {
                if (passenger instanceof Player player) {
                    applyVelocityAndRunTask(livingEntity, player, getVector(event.getBlock(), player));
                    return;
                }
            }

            if (livingEntity instanceof Player player) {
                applyVelocityAndRunTask(player, player, getVector(event.getBlock(), player));
            }
        }

        private void applyVelocityAndRunTask(LivingEntity livingEntity, Player player, Vector vector) {
            applyVelocityWithSound(livingEntity, player, vector);

            AtomicBoolean cancel = new AtomicBoolean(false);
            for (int i = 1; i <= 120; i++) {
                if (cancel.get()) break;
                JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> {
                    if (livingEntity.isOnGround()) {
                        cancel.set(true);
                        return;
                    }
                    Vector currentVelocity = livingEntity.getVelocity();
                    double currentY = currentVelocity.getY();
                    livingEntity.setVelocity(currentVelocity.clone().add(player.getEyeLocation().getDirection().clone().multiply(0.1)).multiply(1.05).setY(currentY));
                    livingEntity.setFallDistance(0);
                    player.setFallDistance(0);
                }, i);
            }
        }

        private void applyVelocityWithSound(@NotNull LivingEntity livingEntity, @NotNull Player player, Vector vector) {
            livingEntity.setVelocity(vector);
            player.playSound(player, Sound.BLOCK_PISTON_EXTEND, 1, 1);
        }
    }
}
