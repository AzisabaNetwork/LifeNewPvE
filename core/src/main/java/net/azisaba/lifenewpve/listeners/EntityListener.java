package net.azisaba.lifenewpve.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.mana.ManaUtil;
import net.azisaba.lifenewpve.utils.CoolTime;
import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntityListener.Interact(), lifeNewPvE);
        pm.registerEvents(new EntityListener.Regain(lifeNewPvE), lifeNewPvE);
        pm.registerEvents(new Damage(), lifeNewPvE);
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

        private final LifeNewPvE lifeNewPvE;

        public Regain(LifeNewPvE lifeNewPvE) {
            this.lifeNewPvE = lifeNewPvE;
        }

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
                ManaUtil.multiplyMana(p, 0.05);
                return;
            }
            if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.EATING) return;
            if (CoolTime.isCoolTime(getClass(), playerId, multimap)) {
                applyCooldownAndAdjustAmount(event, playerHealth, playerId);
                ManaUtil.multiplyMana(p, 0.2);
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
            lifeNewPvE.runAsyncDelayed(() -> countMap.merge(playerId, -1, Integer::sum), 200);
        }
    }

    public static class Damage implements Listener {

        @EventHandler
        public void onDamage(@NotNull EntityDamageEvent event) {
            if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
            if (!event.getCause().equals(EntityDamageEvent.DamageCause.POISON)) return;
            if (!livingEntity.hasPotionEffect(PotionEffectType.POISON)) return;

            PotionEffect effect = livingEntity.getPotionEffect(PotionEffectType.POISON);
            int level = Objects.requireNonNull(effect).getAmplifier();

            event.setDamage(event.getDamage() + Math.sqrt(level));
        }
    }
}
