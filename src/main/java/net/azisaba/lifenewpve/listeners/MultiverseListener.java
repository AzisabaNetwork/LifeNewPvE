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
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiverseListener implements Listener {

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MultiverseListener.Delete(plugin), plugin);
    }

    public static void settings(@NotNull World w, Difficulty dif) {
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        w.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        w.setGameRule(GameRule.KEEP_INVENTORY, true);
        w.setGameRule(GameRule.SPAWN_RADIUS, 0);
        w.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
        w.setPVP(false);
        w.setDifficulty(dif);
        w.getWorldBorder().setCenter(0, 0);
        w.getWorldBorder().setSize(10000);
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
            Map<Flag<?>, Object> flags = new HashMap<>(rg.getFlags());
            plugin.runSyncDelayed(()-> regenWorld(name, gen, difficulty, type, environment), 50);
            plugin.runSyncDelayed(()-> addWorldGuard(bukkit, flags), 200);
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
                        MythicListener.reloadMythic(20);
                    }
                }, 80);
            } else {
                plugin.getLogger().info(name + "の生成に失敗しました。");
            }
        }

        private void addWorldGuard(World w, Map<Flag<?>, Object> flags) {
            try {
                if (flags == null || flags.isEmpty()) return;
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(w);
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
                if (manager == null) return;

                ProtectedRegion reg = manager.getRegion(ProtectedRegion.GLOBAL_REGION);
                if (reg == null) return;

                reg.setFlags(flags);
                List<ProtectedRegion> list = new ArrayList<>(Collections.singleton(reg));

                manager.setRegions(list);
                manager.save();
            } catch (StorageException ignored) {
            }
        }
    }
}
