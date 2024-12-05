package net.azisaba.lifenewpve.listeners.entity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.mana.ManaUtil;
import net.azisaba.lifenewpve.utils.CoolTime;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityRegainListener extends EntityListener {

    private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

    private static final Map<UUID, Integer> countMap = new HashMap<>();

    private final LifeNewPvE lifeNewPvE;

    public EntityRegainListener(LifeNewPvE lifeNewPvE) {
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
