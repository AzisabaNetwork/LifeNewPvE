package net.azisaba.lifenewpve.listeners.player;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class PlayerListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerCommandListener(), lifeNewPvE);
        pm.registerEvents(new PlayerQuitListener(lifeNewPvE), lifeNewPvE);
        pm.registerEvents(new PlayerChangeWorldListener(), lifeNewPvE);
        pm.registerEvents(new PlayerInteractListener(), lifeNewPvE);
        pm.registerEvents(new PlayerPreAttackListener(), lifeNewPvE);
        pm.registerEvents(new PlayerJoinListener(lifeNewPvE), lifeNewPvE);
    }
}
