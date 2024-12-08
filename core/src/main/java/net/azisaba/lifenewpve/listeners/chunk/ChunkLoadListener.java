package net.azisaba.lifenewpve.listeners.chunk;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ChunkLoadListener extends ChunkListener {

    private final LifeNewPvE lifeNewPvE;

    public ChunkLoadListener(LifeNewPvE lifeNewPvE) {
        this.lifeNewPvE = lifeNewPvE;
    }

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent e) {
        Chunk chunk = e.getChunk();
        JavaPlugin.getPlugin(LifeNewPvE.class).runAsync(() -> {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 50; y < 150; y++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getType() == Material.POWDER_SNOW) {
                            lifeNewPvE.runSync(() -> block.setType(Material.SNOW_BLOCK));
                        } else if (block.getType() == Material.TALL_GRASS) {
                            lifeNewPvE.runSync(() -> block.setType(Material.AIR));
                        }
                    }
                }
            }
        });
    }
}
