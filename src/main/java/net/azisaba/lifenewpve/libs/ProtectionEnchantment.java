package net.azisaba.lifenewpve.libs;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ProtectionEnchantment implements InventoryHolder {

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, Component.text("§3§lエンチャント保護機能。§f§l保護アイテムを選択後、保護するエンチャントをクリック。"));
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, getEnchantBack());
        }

        for (int i = 36; i < 45; i++) {
            inv.setItem(i, getLine());
        }

        for (int i = 45; i < inv.getSize(); i++) {
            inv.setItem(i, getBackStep1());
        }

        return inv;
    }

    public ItemStack getLine() {
        return new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    }

    public ItemStack getBackStep1() {
        return new ItemStack(Material.RED_STAINED_GLASS_PANE);
    }

    public ItemStack getEnchantBack() {
        return new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    }

    public static void nextStep(Inventory inv) {
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
        }
    }
}
