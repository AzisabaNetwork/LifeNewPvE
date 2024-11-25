package net.azisaba.lifenewpve.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.CoolTime;
import net.azisaba.lifenewpve.libs.mana.Mana;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntityListener.Interact(), lifeNewPvE);
        pm.registerEvents(new EntityListener.Regain(), lifeNewPvE);
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

    public static class Regain extends EntityListener {

        private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

        private static final Map<UUID, Integer> countMap = new HashMap<>();

        @EventHandler
        public void onRegainHealth(@NotNull EntityRegainHealthEvent event) {
            if (!(event.getEntity() instanceof Player p)) return;
            double playerHealth = extractPlayerHealth(event);
            UUID playerId = p.getUniqueId();

            if (countMap.getOrDefault(playerId, 0) >= 5) {
                return;
            }
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                adjustEventAmount(event, playerHealth, playerId);
                Mana.modifyMana(p, 0.05, ManaModifyEvent.Type.EATING);
                return;
            }
            if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.EATING) return;
            if (CoolTime.isCoolTime(getClass(), playerId, multimap)) {
                applyCooldownAndAdjustAmount(event, playerHealth, playerId);
                Mana.modifyMana(p, 0.2, ManaModifyEvent.Type.EATING);
            }
        }

        private double extractPlayerHealth(@NotNull EntityRegainHealthEvent event) {
            AttributeInstance attr = ((Player) event.getEntity()).getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            return attr == null ? 1 : attr.getValue();
        }

        private void applyCooldownAndAdjustAmount(@NotNull EntityRegainHealthEvent event, double playerHealth, UUID playerId) {
            Class<?> clazz = getClass();
            CoolTime.setCoolTime(clazz, playerId, multimap, 60);
            event.setAmount(event.getAmount() * 0.1 * playerHealth + 2);
            healCount(playerId);
        }

        private void adjustEventAmount(@NotNull EntityRegainHealthEvent event, double playerHealth, UUID playerId) {
            event.setAmount(event.getAmount() * 0.05 * playerHealth + 1);
            healCount(playerId);
        }

        private void healCount(UUID playerId) {
            countMap.merge(playerId, 1, Integer::sum);
            JavaPlugin.getPlugin(LifeNewPvE.class).runAsyncDelayed(() -> countMap.merge(playerId, -1, Integer::sum), 200);
        }
    }
}
