package net.azisaba.lifenewpve.libs.potion;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.event.PotionEffectEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class PotionTimer extends BukkitRunnable {

    private final LifeNewPvE plugin;

    private final LivingEntity living;

    public PotionTimer(LifeNewPvE plugin, LivingEntity living) {
        this.plugin = plugin;
        this.living = living;
    }

    private static final int max_potions = 20;

    @Override
    public void run() {
        stop();
        PersistentDataContainer pc = living.getPersistentDataContainer();
        List<NamespacedKey> rem = new ArrayList<>();

        for (int i = 0; i < max_potions; i++) {
            if (!pc.has(new NamespacedKey(plugin, "potions_" + i), PersistentDataType.STRING)) continue;
            NamespacedKey key = new NamespacedKey(plugin, "potions_" + i);
            String get = pc.get(key, PersistentDataType.STRING);
            if (get == null) continue;

            String[] split = get.split(":");
            String data = split[0];
            int level = Integer.parseInt(split[1]);
            long seconds = Long.parseLong(split[2]);

            seconds--;
            boolean remove;
            if (seconds <= 0) {
                rem.add(key);
                remove = true;
            } else {
                pc.set(key, PersistentDataType.STRING, data + ":" + level + ":" + seconds);
                remove = false;
            }
            new PotionEffectEvent(living, data, level, seconds, remove).callEvent();
        }

        for (NamespacedKey key : rem) {
            pc.remove(key);
        }
    }

    public static long getMaxPotions() {
        return max_potions;
    }

    private void stop() {
        if (living != null) {
            for (int i = 0; i < max_potions; i++) {
                if (!living.getPersistentDataContainer().has(new NamespacedKey(plugin, "potions_" + i), PersistentDataType.STRING)) {
                    continue;
                }
                return;
            }
            LifePotion.stop(living.getUniqueId());
        }
        cancel();
    }
}
