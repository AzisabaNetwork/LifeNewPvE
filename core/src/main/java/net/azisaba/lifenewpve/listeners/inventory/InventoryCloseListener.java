package net.azisaba.lifenewpve.listeners.inventory;

import net.azisaba.lifenewpve.libs.enchantments.ProtectionEnchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryCloseListener extends InventoryListener {

    @EventHandler
    public void onProtectEnchantment(@NotNull InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (!(inv.getHolder() instanceof ProtectionEnchantment)) return;
        ItemStack item = inv.getItem(49);
        if (item == null || !item.hasItemMeta()) return;

        HumanEntity p = e.getPlayer();
        for (ItemStack i : p.getInventory().addItem(item).values()) {
            p.getWorld().dropItem(p.getLocation(), i);
        }
    }
}
