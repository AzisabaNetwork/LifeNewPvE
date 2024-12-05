package net.azisaba.lifenewpve.listeners.entity;

import net.azisaba.lifenewpve.libs.VectorTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityInteractEvent;
import org.jetbrains.annotations.NotNull;

public class EntityInteractListener extends EntityListener implements VectorTask {

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
