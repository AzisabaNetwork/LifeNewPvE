package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.Mana;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManaListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm  = Bukkit.getPluginManager();
        pm.registerEvents(new ManaListener.Modify(), lifeNewPvE);
    }

    public static class Modify extends ManaListener {

        private static final Map<UUID, KeyedBossBar> BOSS_BAR = new HashMap<>();

        private static final Map<UUID, Integer> VISIBLE_COUNT = new ConcurrentHashMap<>();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onManaModify(@NotNull ManaModifyEvent event) {
            Player p = event.getPlayer();
            if (BOSS_BAR.containsKey(p.getUniqueId())) {
                updateBossBar(p);
            } else {
                createBossBar(p);
            }
        }

        private static double getProgress(@NotNull Player p) {
            return getMin(p) / getMax(p);
        }

        private static double getMin(@NotNull Player p) {
            return Math.min(Mana.getMana(p.getPersistentDataContainer()), getMax(p));
        }

        private static double getMax(@NotNull Player p) {
            return Mana.getMaxMana(p.getPersistentDataContainer());
        }

        private static void subtractVisibleCount(UUID playerId) {
            JavaPlugin.getPlugin(LifeNewPvE.class).runAsyncDelayed(() -> {
                VISIBLE_COUNT.merge(playerId, -1, Integer::sum);
                if (VISIBLE_COUNT.getOrDefault(playerId, 0) <= 0) {
                    removeBossBar(playerId);
                } else {
                    if (VISIBLE_COUNT.get(playerId) > 5) {
                        VISIBLE_COUNT.put(playerId, 5);
                    }
                    subtractVisibleCount(playerId);
                }
            }, 20);
        }

        public static void createBossBar(@NotNull Player p) {
            NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), p.getUniqueId().toString().toLowerCase());
            KeyedBossBar keyed = Bukkit.createBossBar(key, "§b§lマナ §f§l" + getMin(p) + " §f/§§l " + getMax(p) , BarColor.BLUE, BarStyle.SEGMENTED_10);
            keyed.setProgress(getProgress(p));
            keyed.addPlayer(p);
            keyed.setVisible(true);
            BOSS_BAR.put(p.getUniqueId(), keyed);
            VISIBLE_COUNT.put(p.getUniqueId(), 3);
            subtractVisibleCount(p.getUniqueId());
        }

        public static void removeBossBar(@NotNull UUID playerId) {
            VISIBLE_COUNT.remove(playerId);
            if (BOSS_BAR.containsKey(playerId)) {
                KeyedBossBar bar = BOSS_BAR.get(playerId);
                bar.setVisible(false);
                bar.removeAll();
                BOSS_BAR.remove(playerId);
            }
        }

        private static void updateBossBar(@NotNull Player p) {
            KeyedBossBar bar = BOSS_BAR.get(p.getUniqueId());
            bar.setTitle("§b§lマナ §f§l" + getMin(p) + " §f/§§l " + getMax(p));
            bar.setProgress(getProgress(p));
            bar.setVisible(true);
            VISIBLE_COUNT.merge(p.getUniqueId(), 3, Integer::sum);
        }

        public static void removeAll() {
            for (KeyedBossBar bar : BOSS_BAR.values()) {
                bar.setVisible(false);
                bar.removeAll();
            }
            BOSS_BAR.clear();
            VISIBLE_COUNT.clear();
        }
    }
}
