package net.azisaba.lifenewpve.listeners.inventory;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class InventoryListener implements Listener {


    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new InventoryClickListener(lifeNewPvE), lifeNewPvE);
        pm.registerEvents(new InventoryCloseListener(), lifeNewPvE);
    }
}
