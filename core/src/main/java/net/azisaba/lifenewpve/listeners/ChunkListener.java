package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ChunkListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChunkListener.Load(lifeNewPvE), lifeNewPvE);
    }

    public static class Load implements Listener {

        private final LifeNewPvE lifeNewPvE;

        public Load(LifeNewPvE lifeNewPvE) {
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
                            if (block.getType() != Material.POWDER_SNOW) continue;
                            lifeNewPvE.runSync(() -> block.setType(Material.SNOW_BLOCK));

                        }
                    }
                }
            });
        }
    }
}
