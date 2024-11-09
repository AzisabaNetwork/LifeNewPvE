package net.azisaba.lifenewpve.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVWorldDeleteEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiverseListener implements Listener {

    private static final Set<String> RESET = new HashSet<>();

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MultiverseListener.Delete(plugin), plugin);
    }

    public static boolean isResetWorld(String name) {
        return RESET.contains(name);
    }

    public static void settings(@NotNull World w, Difficulty dif) {
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        w.setGameRule(GameRule.KEEP_INVENTORY, true);
        w.setGameRule(GameRule.SPAWN_RADIUS, 0);
        w.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
        w.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false);
        w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        w.setPVP(false);
        w.setDifficulty(dif);
        w.setSpawnLocation(0, 64, 0);
        w.getWorldBorder().setCenter(0, 0);

        if (!w.getName().contains("resource")) {
            w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            w.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
            w.setGameRule(GameRule.DISABLE_RAIDS, true);
            w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            w.setViewDistance(6);
            w.setSimulationDistance(4);
        } else {
            w.getWorldBorder().setSize(50);
            w.getWorldBorder().setSize(10000, 3600);
            w.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 7);
            w.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 1);
            w.setViewDistance(8);
            w.setSimulationDistance(4);
        }
    }

    public static class Delete extends MultiverseListener {

        private final LifeNewPvE plugin;

        public Delete(LifeNewPvE plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onDelete(@NotNull MVWorldDeleteEvent e) {
            MultiverseWorld w = e.getWorld();
            String name = w.getName();
            if (!name.toLowerCase().contains("resource")) return;
            RESET.add(name);

            World bukkit = w.getCBWorld();
            WorldType type = w.getWorldType();
            String gen = w.getGenerator();
            World.Environment environment = w.getEnvironment();
            Difficulty difficulty = w.getDifficulty();

            com.sk89q.worldedit.world.World edit = BukkitAdapter.adapt(bukkit);
            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(edit);
            if (manager == null) return;

            ProtectedRegion rg = manager.getRegion(ProtectedRegion.GLOBAL_REGION);
            if (rg == null) return;

            ProtectedRegion rgs = manager.getRegion("spawn");
            if (rgs == null) return;
            plugin.runSyncDelayed(()-> regenWorld(name, gen, difficulty, type, environment), 50);
            plugin.runSyncDelayed(()-> addWorldGuard(bukkit, new HashMap<>(rg.getFlags()), new HashMap<>(rgs.getFlags())), 200);
        }

        private void regenWorld(String name, String gen, Difficulty dif, WorldType type, World.Environment environment) {
            MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
            if (core.getMVWorldManager()
                    .addWorld(name, environment, UUID.randomUUID().toString(), type, true, gen)) {

                plugin.runSyncDelayed(()-> {
                    World w = core.getMVWorldManager().getMVWorld(name).getCBWorld();
                    if (w == null) return;
                    settings(w, dif);
                    plugin.getLogger().info(name + "の生成に成功しました。");

                    if (MythicListener.isMythic()) {
                        MythicListener.reloadMythic(100);
                    }
                    chunkLoad(w);
                    RESET.remove(name);
                }, 20);
            } else {
                plugin.getLogger().info(name + "の生成に失敗しました。");
                RESET.remove(name);
            }
        }

        private void chunkLoad(@NotNull World w) {
            plugin.runSyncDelayed(()-> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius 50");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start " + w.getName());
                Bukkit.broadcast(Component.text("§b§l事前チャンクロードを準備中です…"));

                plugin.runSyncDelayed(()-> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fcp start 2000 " + w.getName() + " 0 0");
                    Bukkit.broadcast(Component.text("§a§l事前チャンクロードを開始しました！"));
                }, 100);
            }, 200);
        }

        private void addWorldGuard(World w, Map<Flag<?>, Object> flags, Map<Flag<?>, Object> spawn) {
            try {
                if (flags == null || flags.isEmpty()) return;
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(w);
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
                if (manager == null) return;

                ProtectedRegion reg = manager.getRegion(ProtectedRegion.GLOBAL_REGION);
                if (reg == null) return;

                ProtectedRegion regs = manager.getRegion("spawn");
                if (regs == null)  return;

                reg.setFlags(flags);
                regs.setFlags(spawn);
                List<ProtectedRegion> list = new ArrayList<>(List.of(reg, regs));

                manager.setRegions(list);
                manager.save();
            } catch (StorageException ignored) {
            }
        }
    }
}
