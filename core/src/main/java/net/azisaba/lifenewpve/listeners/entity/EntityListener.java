package net.azisaba.lifenewpve.listeners.entity;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class EntityListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntityInteractListener(), lifeNewPvE);
        pm.registerEvents(new EntityRegainListener(lifeNewPvE), lifeNewPvE);
        pm.registerEvents(new EntityDamageListener(), lifeNewPvE);
    }
}
