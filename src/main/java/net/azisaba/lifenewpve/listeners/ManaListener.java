package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.event.ManaModifiedEvent;
import net.azisaba.lifenewpve.libs.mana.Mana;
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
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ManaListener.Modify(), lifeNewPvE);
    }

    public static class Modify extends ManaListener {
        private static final Map<UUID, KeyedBossBar> bossBarMap = new HashMap<>();
        private static final Map<UUID, Integer> visibleCountMap = new ConcurrentHashMap<>();
        private static final int MAX_VISIBLE_THRESHOLD = 5;
        private static final int INITIAL_DELAY = 20;

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onManaModify(@NotNull ManaModifiedEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            if (bossBarMap.containsKey(playerId)) {
                updateBossBar(player);
            } else {
                createBossBar(player);
            }
        }

        private static double getProgress(@NotNull Player player) {
            return getMinMana(player) / getMaxMana(player);
        }

        private static double getMinMana(@NotNull Player player) {
            return Math.min(Mana.getMana(player.getPersistentDataContainer()), getMaxMana(player));
        }

        private static double getMaxMana(@NotNull Player player) {
            return Mana.getMaxMana(player, player.getPersistentDataContainer());
        }

        private static void subtractVisibleCount(UUID playerId) {
            JavaPlugin.getPlugin(LifeNewPvE.class).runAsyncDelayed(() -> {
                decrementVisibleCount(playerId);
                ensureVisibleCountWithinLimits(playerId);
                handleBossBarVisibility(playerId);
            }, INITIAL_DELAY);
        }

        private static void decrementVisibleCount(UUID playerId) {
            visibleCountMap.merge(playerId, -1, Integer::sum);
        }

        private static void ensureVisibleCountWithinLimits(UUID playerId) {
            visibleCountMap.computeIfPresent(playerId, (uuid, count) -> Math.min(count, MAX_VISIBLE_THRESHOLD));
        }

        private static void handleBossBarVisibility(UUID playerId) {
            if (getVisibleCount(playerId) <= 0) {
                removeBossBar(playerId);
            } else {
                rescheduleVisibilityCheck(playerId);
            }
        }

        private static void rescheduleVisibilityCheck(UUID playerId) {
            if (getVisibleCount(playerId) > MAX_VISIBLE_THRESHOLD) {
                visibleCountMap.put(playerId, MAX_VISIBLE_THRESHOLD);
            }
            subtractVisibleCount(playerId);
        }

        private static int getVisibleCount(UUID playerId) {
            return visibleCountMap.getOrDefault(playerId, 0);
        }

        public static void createBossBar(@NotNull Player player) {
            UUID playerId = player.getUniqueId();
            NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), playerId.toString().toLowerCase());
            KeyedBossBar keyedBossBar = Bukkit.createBossBar(key, createBossBarTitle(player), BarColor.BLUE, BarStyle.SEGMENTED_10);
            keyedBossBar.setProgress(getProgress(player));
            keyedBossBar.addPlayer(player);
            keyedBossBar.setVisible(true);
            bossBarMap.put(playerId, keyedBossBar);
            visibleCountMap.put(playerId, 3);
            subtractVisibleCount(playerId);
        }

        @NotNull
        private static String createBossBarTitle(@NotNull Player player) {
            return "§b§lマナ §f§l" + getMinMana(player) + " §f/§f§l " + getMaxMana(player);
        }

        public static void removeBossBar(@NotNull UUID playerId) {
            visibleCountMap.remove(playerId);
            if (bossBarMap.containsKey(playerId)) {
                KeyedBossBar bar = bossBarMap.get(playerId);
                bar.setVisible(false);
                bar.removeAll();
                bossBarMap.remove(playerId);
            }
        }

        private static void updateBossBar(@NotNull Player player) {
            UUID playerId = player.getUniqueId();
            KeyedBossBar bar = bossBarMap.get(playerId);
            bar.setTitle(createBossBarTitle(player));
            bar.setProgress(getProgress(player));
            bar.setVisible(true);
            visibleCountMap.merge(playerId, 3, Integer::sum);
        }

        public static void removeAll() {
            for (KeyedBossBar bar : bossBarMap.values()) {
                bar.setVisible(false);
                bar.removeAll();
            }
            bossBarMap.clear();
            visibleCountMap.clear();
        }
    }
}
