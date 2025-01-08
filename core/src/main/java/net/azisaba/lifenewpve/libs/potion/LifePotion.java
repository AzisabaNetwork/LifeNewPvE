package net.azisaba.lifenewpve.libs.potion;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LifePotion {

    private final LifeNewPvE plugin;

    private final LivingEntity living;

    private static final Set<UUID> ID = new HashSet<>();

    public LifePotion(LifeNewPvE plugin, LivingEntity living) {
        this.plugin = plugin;
        this.living = living;
    }

    public void init() {
        if (ID.contains(living.getUniqueId())) return;
        PotionTimer timer = new PotionTimer(plugin, living);
        timer.runTaskTimerAsynchronously(plugin, 20, 20);
        ID.add(living.getUniqueId());
    }

    public static void stop(UUID uuid) {
        ID.remove(uuid);
    }

    public List<String> getPotionsData() {
        List<String> list = new ArrayList<>();
        PersistentDataContainer pc = living.getPersistentDataContainer();

        for (NamespacedKey key : pc.getKeys()) {
            if (!key.namespace().equals(plugin.getName())) continue;
            if (!key.value().contains("potions_")) continue;

            String s = pc.get(key, PersistentDataType.STRING);
            if (s == null) continue;
            list.add(s);
        }
        return list;
    }

    public boolean addPotion(String data, int level, long seconds) {
        PersistentDataContainer pc = living.getPersistentDataContainer();
        for (int i = 0; i < PotionTimer.getMaxPotions(); i++) {
            NamespacedKey key = new NamespacedKey(plugin, "potions_" + i);
            if (!pc.has(key, PersistentDataType.STRING)) {
                pc.set(key, PersistentDataType.STRING, data + ":" + level + ":" + seconds);
                return true;
            }
        }
        return false;
    }

    public boolean addPotion(@NotNull Type type, int level, long seconds) {
        return addPotion(type.name(), level, seconds);
    }

    public enum Type {
        DEFENSE,
        ARROW_DAMAGE;

        @Nullable
        public static Type fromString(String s) {
            for (Type type : values()) {
                if (type.name().equalsIgnoreCase(s)) return type;
            }
            return null;
        }
    }
}
