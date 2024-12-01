package net.azisaba.lifenewpve.mana.listener;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.mana.event.ManaModifiedEvent;
import net.azisaba.lifenewpve.mana.ManaBase;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManaListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ManaListener.Modify(lifeNewPvE), lifeNewPvE);
    }

    public static final class Modify extends ManaListener {

        private static final Map<UUID, KeyedBossBar> bossBarMap = new HashMap<>();
        private static final Map<UUID, Integer> visibleCountMap = new ConcurrentHashMap<>();
        private static final int MAX_VISIBLE_THRESHOLD = 5;
        private static final int INITIAL_DELAY = 20;

        private final LifeNewPvE lifeNewPvE;

        public Modify(LifeNewPvE lifeNewPvE) {
            this.lifeNewPvE = lifeNewPvE;
        }

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

        private double getProgress(@NotNull Player player) {
            ManaBase mana = new ManaBase(player, lifeNewPvE);
            return mana.getMana() / mana.getMaxMana();
        }

        private void subtractVisibleCount(UUID playerId) {
            lifeNewPvE.runAsyncDelayed(() -> {
                decrementVisibleCount(playerId);
                ensureVisibleCountWithinLimits(playerId);
                handleBossBarVisibility(playerId);
            }, INITIAL_DELAY);
        }

        private void decrementVisibleCount(UUID playerId) {
            visibleCountMap.merge(playerId, -1, Integer::sum);
        }

        private void ensureVisibleCountWithinLimits(UUID playerId) {
            visibleCountMap.computeIfPresent(playerId, (uuid, count) -> Math.min(count, MAX_VISIBLE_THRESHOLD));
        }

        private void handleBossBarVisibility(UUID playerId) {
            if (getVisibleCount(playerId) <= 0) {
                removeBossBar(playerId);
            } else {
                rescheduleVisibilityCheck(playerId);
            }
        }

        private void rescheduleVisibilityCheck(UUID playerId) {
            if (getVisibleCount(playerId) > MAX_VISIBLE_THRESHOLD) {
                visibleCountMap.put(playerId, MAX_VISIBLE_THRESHOLD);
            }
            subtractVisibleCount(playerId);
        }

        private int getVisibleCount(UUID playerId) {
            return visibleCountMap.getOrDefault(playerId, 0);
        }

        public void createBossBar(@NotNull Player player) {
            UUID playerId = player.getUniqueId();
            NamespacedKey key = lifeNewPvE.getKey().getOrCreate(playerId.toString().toLowerCase());
            KeyedBossBar keyedBossBar = Bukkit.createBossBar(key, createBossBarTitle(player), BarColor.BLUE, BarStyle.SEGMENTED_10);
            barFlags(keyedBossBar, player);
            bossBarMap.put(playerId, keyedBossBar);
            visibleCountMap.put(playerId, 3);
            subtractVisibleCount(playerId);
        }

        private void barFlags(@NotNull KeyedBossBar keyedBossBar, @NotNull Player player) {
            keyedBossBar.setProgress(getProgress(player));
            keyedBossBar.addPlayer(player);
            keyedBossBar.setVisible(true);
        }

        @NotNull
        private String createBossBarTitle(@NotNull Player player) {
            ManaBase mana = new ManaBase(player, lifeNewPvE);
            return "§b§lマナ §f§l" + mana.getMana() + " §f/§f§l " + mana.getMaxMana();
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

        private void updateBossBar(@NotNull Player player) {
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
