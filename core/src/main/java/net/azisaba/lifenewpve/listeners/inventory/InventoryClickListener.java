package net.azisaba.lifenewpve.listeners.inventory;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.enchantments.ProtectionEnchantment;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryClickListener extends InventoryListener {


    private final LifeNewPvE lifeNewPvE;

    public InventoryClickListener(LifeNewPvE lifeNewPvE) {
        this.lifeNewPvE = lifeNewPvE;
    }

    @EventHandler
    public void onSelectProtectEnchantment(@NotNull InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (!(inventory.getHolder() instanceof ProtectionEnchantment)) return;
        e.setCancelled(true);

        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) return;
        if (!(clickedInventory.getHolder() instanceof ProtectionEnchantment)) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.ENCHANTED_BOOK) return;
        if (!(clickedItem.getItemMeta() instanceof EnchantmentStorageMeta meta)) return;
        if (!meta.hasStoredEnchants()) return;

        for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            ItemStack set = inventory.getItem(49);
            if (set == null || !set.hasItemMeta()) return;

            ItemMeta data = set.getItemMeta();
            String s = enchantment.getKey().namespace() + ":" + enchantment.getKey().value() + ":" + level;
            data.getPersistentDataContainer().set(new NamespacedKey(lifeNewPvE, "protect_enchantment"), PersistentDataType.STRING, s);
            set.setItemMeta(data);
            for (int i = 0; i < 36; i++) {
                inventory.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            }
            e.getWhoClicked().sendMessage(Component.text("§a§l最後の操作が完了しました。§f§lデータ §f-> " + s));
            break;
        }


    }

    @EventHandler
    public void onProtectEnchantment(@NotNull InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (!(inventory.getHolder() instanceof ProtectionEnchantment)) return;
        e.setCancelled(true);

        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) return;
        if (clickedInventory.getHolder() instanceof ProtectionEnchantment) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.BOOK ||
                clickedItem.getType() == Material.ENCHANTED_BOOK ||
                !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta stored) {
            if (!stored.hasStoredEnchants()) return;

            setItemStack(clickedItem, inventory);
            List<ItemStack> list = getStored(stored);
            for (int i = 0; i < list.size(); i++) {
                inventory.setItem(i, list.get(i));
            }


        } else {
            if (!meta.hasEnchants()) return;

            setItemStack(clickedItem, inventory);
            List<ItemStack> list = getEnchantment(meta);
            for (int i = 0; i < list.size(); i++) {
                inventory.setItem(i, list.get(i));
            }
        }
        ProtectionEnchantment.nextStep(inventory);
        e.getWhoClicked().sendMessage(Component.text("§b§l操作が完了しました。"));
    }

    private void setItemStack(ItemStack clicked, @NotNull Inventory inv) {
        inv.setItem(49, clicked);
        clicked.setAmount(0);
    }

    @NotNull
    private List<ItemStack> getStored(@NotNull EnchantmentStorageMeta meta) {
        List<ItemStack> stored = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> map : meta.getStoredEnchants().entrySet()) {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta data = (EnchantmentStorageMeta) item.getItemMeta();
            data.addStoredEnchant(map.getKey(), map.getValue(), true);
            item.setItemMeta(data);
            stored.add(item);
        }
        return stored;
    }

    @NotNull
    private List<ItemStack> getEnchantment(@NotNull ItemMeta meta) {
        List<ItemStack> stored = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> map : meta.getEnchants().entrySet()) {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta data = (EnchantmentStorageMeta) item.getItemMeta();
            data.addStoredEnchant(map.getKey(), map.getValue(), true);
            item.setItemMeta(data);
            stored.add(item);
        }
        return stored;
    }
}
