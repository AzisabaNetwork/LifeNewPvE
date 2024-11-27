package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
        pm.registerEvents(new PrepareListener.Anvil(lifeNewPvE), lifeNewPvE);

    }

    public static class Grindstone extends PrepareListener {

        private final LifeNewPvE lifeNewPvE;

        public Grindstone(LifeNewPvE lifeNewPvE) {
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

    public static class Anvil extends PrepareListener {

        private final LifeNewPvE lifeNewPvE;

        public Anvil(LifeNewPvE lifeNewPvE) {
            super(lifeNewPvE);
            this.lifeNewPvE = lifeNewPvE;
        }

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
        public void onAnvilPrepare(@NotNull PrepareAnvilEvent event) {
            ItemStack one = event.getInventory().getFirstItem();
            if (isDeny(one)) {
                event.setResult(null);
                //武器 + 武器　不可
                //武器 + 本　可能
                //本 + 武器 不可
                //本 + 本 を禁止する。
            }
        }

        private boolean isDeny(ItemStack item) {
            if (item == null || !item.hasItemMeta()) return false;
            return item.getItemMeta()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(lifeNewPvE, "deny_book_merge"), PersistentDataType.BOOLEAN);
        }
    }
}
