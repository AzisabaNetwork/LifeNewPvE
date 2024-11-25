package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class PrepareListener implements Listener {

    private final LifeNewPvE lifeNewPvE;

    public PrepareListener(LifeNewPvE lifeNewPvE) {
        this.lifeNewPvE = lifeNewPvE;
    }

    public void initialize() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PrepareListener.Grindstone(lifeNewPvE), lifeNewPvE);
    }

    public static class Grindstone extends PrepareListener {

        private final LifeNewPvE lifeNewPvE;

        public Grindstone(LifeNewPvE lifeNewPvE) {
            super(lifeNewPvE);
            this.lifeNewPvE = lifeNewPvE;
        }

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
        public void onGrindstonePrepare(@NotNull PrepareGrindstoneEvent event) {
            ItemStack result = event.getResult();
            if (result == null || result.getType() == Material.AIR) return;
            for (ItemStack item : event.getInventory().getContents()) {
                if (item == null || !item.hasItemMeta()) continue;
                PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
                if (dataContainer.has(new NamespacedKey(lifeNewPvE, "prevent_grindstone"), PersistentDataType.STRING)) {
                    event.setResult(null);
                    break;
                }
            }
        }
    }
}
