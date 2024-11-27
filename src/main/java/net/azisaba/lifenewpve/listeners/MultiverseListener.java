package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiverseListener implements Listener {

    protected static final Set<String> RESET_WORLD_NAMES = new HashSet<>();

    public void initialize(LifeNewPvE plugin) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new MultiverseWorldDeleteListener(plugin), plugin);
    }

    public static boolean isResetWorld(String worldName) {
        return RESET_WORLD_NAMES.contains(worldName);
    }

    public static void configureWorldSettings(@NotNull World world, Difficulty difficulty) {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
        world.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setPVP(false);
        world.setDifficulty(difficulty);
        world.setSpawnLocation(0, 64, 0);
        world.getWorldBorder().setCenter(0, 0);

        if (!world.getName().contains("resource")) {
            configureWorldNotResourceSettings(world);
        } else {
            configureWorldResourceSettings(world);
        }
    }

    private static void configureWorldNotResourceSettings(@NotNull World world) {
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setViewDistance(6);
        world.setSimulationDistance(4);
    }

    private static void configureWorldResourceSettings(@NotNull World world) {
        world.getWorldBorder().setSize(50);
        world.getWorldBorder().setSize(10000, 3600);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 7);
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 1);
        world.setViewDistance(8);
        world.setSimulationDistance(4);
    }
}
