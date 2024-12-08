package net.azisaba.lifenewpve.listeners.block;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class BlockListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockPlaceListener(lifeNewPvE), lifeNewPvE);
    }
}
