package net.azisaba.lifenewpve.listeners.prepare;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PrepareGrindListener extends PrepareListener {

    private final LifeNewPvE lifeNewPvE;

    public PrepareGrindListener(LifeNewPvE lifeNewPvE) {
        super(lifeNewPvE);
        this.lifeNewPvE = lifeNewPvE;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrindstonePrepare(@NotNull PrepareGrindstoneEvent event) {
        ItemStack result = event.getResult();
        if (result == null || result.getType() == Material.AIR) return;
        if (event.getInventory().getContents().length != 1) return;
        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if (dataContainer.has(new NamespacedKey(lifeNewPvE, "prevent_grindstone"), PersistentDataType.STRING)) {
                event.setResult(null);
                break;
            } else if (dataContainer.has(new NamespacedKey(lifeNewPvE, "deny_book_merge"))) {

                dataContainer.remove(new NamespacedKey(lifeNewPvE, "deny_book_merge"));
                item.setItemMeta(meta);
                event.setResult(item);
            }
        }
    }
}
