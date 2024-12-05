package net.azisaba.lifenewpve.listeners.prepare;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PrePareAnvilListener extends PrepareListener {

    private final LifeNewPvE lifeNewPvE;

    public PrePareAnvilListener(LifeNewPvE lifeNewPvE) {
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
