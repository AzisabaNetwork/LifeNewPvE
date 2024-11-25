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
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiverseWorldDeleteListener extends MultiverseListener {

    private final LifeNewPvE plugin;

    public MultiverseWorldDeleteListener(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDelete(@NotNull MVWorldDeleteEvent event) {
        MultiverseWorld world = event.getWorld();
        String worldName = world.getName();
        if (!worldName.toLowerCase().contains("resource")) return;

        RESET_WORLD_NAMES.add(worldName);
        World bukkitWorld = world.getCBWorld();
        WorldType worldType = world.getWorldType();
        String generator = world.getGenerator();
        World.Environment environment = world.getEnvironment();
        Difficulty difficulty = world.getDifficulty();

        com.sk89q.worldedit.world.World editWorld = BukkitAdapter.adapt(bukkitWorld);
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(editWorld);
        if (regionManager == null) return;

        ProtectedRegion globalRegion = regionManager.getRegion(ProtectedRegion.GLOBAL_REGION);
        if (globalRegion == null) return;

        ProtectedRegion spawnRegion = regionManager.getRegion("spawn");
        if (spawnRegion == null) return;

        plugin.runSyncDelayed(() -> regenerateWorld(worldName, generator, difficulty, worldType, environment), 50);
        plugin.runSyncDelayed(() -> applyWorldGuardFlags(bukkitWorld, new HashMap<>(globalRegion.getFlags()), new HashMap<>(spawnRegion.getFlags())), 200);
    }

    private void regenerateWorld(String worldName, String generator, Difficulty difficulty, WorldType worldType, World.Environment environment) {
        MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
        if (core.getMVWorldManager().addWorld(worldName, environment, UUID.randomUUID().toString(), worldType, true, generator)) {
            plugin.runSyncDelayed(() -> {
                World world = core.getMVWorldManager().getMVWorld(worldName).getCBWorld();
                if (world == null) return;
                configureWorldSettings(world, difficulty);
                plugin.getLogger().info(worldName + "の生成に成功しました。");
                if (MythicListener.isMythic()) {
                    MythicListener.reloadMythic(100);
                }
                loadChunks(world);
                RESET_WORLD_NAMES.remove(worldName);
            }, 20);
        } else {
            plugin.getLogger().info(worldName + "の生成に失敗しました。");
            RESET_WORLD_NAMES.remove(worldName);
        }
    }

    private void loadChunks(@NotNull World world) {
        plugin.runSyncDelayed(() -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius 50");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start " + world.getName());
            Bukkit.broadcast(Component.text("§b§l事前チャンクロードを準備中です…"));
            plugin.runSyncDelayed(() -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fcp start 2000 " + world.getName() + " 0 0");
                Bukkit.broadcast(Component.text("§a§l事前チャンクロードを開始しました！"));
            }, 100);
        }, 200);
    }

    private void applyWorldGuardFlags(World world, Map<Flag<?>, Object> globalFlags, Map<Flag<?>, Object> spawnFlags) {
        try {
            if (globalFlags == null || globalFlags.isEmpty()) return;

            com.sk89q.worldedit.world.World editWorld = BukkitAdapter.adapt(world);
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(editWorld);
            if (regionManager == null) return;

            ProtectedRegion globalRegion = regionManager.getRegion(ProtectedRegion.GLOBAL_REGION);
            if (globalRegion == null) return;

            ProtectedRegion spawnRegion = regionManager.getRegion("spawn");
            if (spawnRegion == null) return;

            globalRegion.setFlags(globalFlags);
            spawnRegion.setFlags(spawnFlags);
            List<ProtectedRegion> regions = Arrays.asList(globalRegion, spawnRegion);
            regionManager.setRegions(regions);
            regionManager.save();
        } catch (StorageException ignored) {
        }
    }
}
