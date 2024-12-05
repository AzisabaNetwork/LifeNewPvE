package net.azisaba.lifenewpve.listeners.prepare;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class PrepareListener implements Listener {

    private final LifeNewPvE lifeNewPvE;

    public PrepareListener(LifeNewPvE lifeNewPvE) {
        this.lifeNewPvE = lifeNewPvE;
    }

    public void initialize() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PrepareGrindListener(lifeNewPvE), lifeNewPvE);
        pm.registerEvents(new PrePareAnvilListener(lifeNewPvE), lifeNewPvE);

    }
}
