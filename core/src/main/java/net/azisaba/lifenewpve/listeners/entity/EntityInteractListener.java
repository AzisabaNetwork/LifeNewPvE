package net.azisaba.lifenewpve.listeners.entity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.utils.CoolTime;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityInteractListener extends EntityListener {

    private static final Multimap<Class<?>, UUID> ct = HashMultimap.create();

    @EventHandler
    public void onEntityInteract(@NotNull EntityInteractEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        for (Entity passenger : livingEntity.getPassengers()) {
            if (passenger instanceof Player player) {
                if (CoolTime.isCoolTime(getClass(), livingEntity.getUniqueId(), ct)) return;
                CoolTime.setCoolTime(getClass(), livingEntity.getUniqueId(), ct, 20);
                VectorTask.applyVelocityAndRunTask(livingEntity, player, VectorTask.getVector(event.getBlock(), player));
                return;
            }
        }
    }
}
