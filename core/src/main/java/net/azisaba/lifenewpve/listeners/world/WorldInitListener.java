package net.azisaba.lifenewpve.listeners.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WorldInitListener extends WorldListener {

    @EventHandler
    public void onWorldInit(@NotNull WorldInitEvent e) {
        World w = e.getWorld();
        if (w.getName().contains("resource")) {
            w.getPopulators().add(new BedrockGen());
        }
    }

    private static class BedrockGen extends BlockPopulator {

        @Override
        public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
            if (!((chunkX == 0 || chunkX == -1) && (chunkZ == 0 || chunkZ == -1))) return;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++) {

                        Material m = y >= 64 ? Material.AIR : Material.BEDROCK;
                        limitedRegion.setBlockData(x + chunkX * 16, y, z + chunkZ * 16, m.createBlockData());
                    }
                }
            }
        }
    }
}
