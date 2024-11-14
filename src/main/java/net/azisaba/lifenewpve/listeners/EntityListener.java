package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.VectorTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class EntityListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntityListener.Interact(), lifeNewPvE);
    }

    public static class Interact extends EntityListener implements VectorTask {


        @EventHandler
        public void onEntityInteract(@NotNull EntityInteractEvent event) {
            if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

            for (Entity passenger : livingEntity.getPassengers()) {
                if (passenger instanceof Player player) {
                    applyVelocityAndRunTask(livingEntity, player, getVector(event.getBlock(), player));
                    return;
                }
            }
        }
    }
}
