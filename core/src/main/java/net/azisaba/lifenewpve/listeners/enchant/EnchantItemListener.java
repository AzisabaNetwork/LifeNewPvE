package net.azisaba.lifenewpve.listeners.enchant;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.enchantments.LifeEnchantment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnchantItemListener extends EnchantListener {

    private final LifeNewPvE plugin;

    public EnchantItemListener(LifeNewPvE lifeNewPvE) {
        this.plugin = lifeNewPvE;

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProtectItemEnchant(@NotNull EnchantItemEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "protect_enchantment");
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String s = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (s == null) return;
            String[] base =  s.split(":");
            String name = base[0];
            String value = base[1];
            String levelS = base[2];
            Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(name, value));
            if (enchantment == null) return;
            int level = Integer.parseInt(levelS);

            meta.getPersistentDataContainer().remove(key);
            item.setItemMeta(meta);
            item.addUnsafeEnchantment(enchantment, level);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItem(@NotNull EnchantItemEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (item.getType() != Material.BOOK) {

            processNonBookItem(event, meta);
            setCustomEnchantment(meta, item);
            item.setItemMeta(meta);

            event.setItem(item);

        } else {
            ItemStack book = item.withType(Material.ENCHANTED_BOOK);
            ItemMeta bookMeta = book.getItemMeta();

            setCustomStoredEnchantment((EnchantmentStorageMeta) bookMeta);
            denyBookMergeEnchantment(bookMeta);
            book.setItemMeta(bookMeta);

            event.setItem(book);
        }
    }

    private void denyBookMergeEnchantment(@NotNull ItemMeta meta) {
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "deny_book_merge"), PersistentDataType.BOOLEAN, true);
    }

    private void processNonBookItem(@NotNull EnchantItemEvent event, ItemMeta meta) {
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();

        enchantsToAdd.keySet().stream()
                .filter(this::canUpgradeEnchantment)
                .forEach(enchantment -> upgradeEnchantmentIfPossible(enchantsToAdd, enchantment));
        for (Map.Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
    }

    private boolean canUpgradeEnchantment(Enchantment enchantment) {
        return ENCHANTMENT_UPGRADES.containsKey(enchantment);
    }


    private void upgradeEnchantmentIfPossible(@NotNull Map<Enchantment, Integer> enchants, Enchantment enchantment) {
        EnchantmentUpgrade upgrade = ENCHANTMENT_UPGRADES.get(enchantment);
        int currentLevel = enchants.get(enchantment);
        int maxLevel = upgrade.maxLevel();
        double upgradeProbability = upgrade.upgradeProbability();

        while (currentLevel < maxLevel && Math.random() < upgradeProbability) {
            currentLevel++;
        }
        enchants.put(enchantment, currentLevel);
    }

    private void setCustomEnchantment(ItemMeta meta, ItemStack item) {
        int repeatSelection = getRandomSelection(1, 8);
        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (int i = 0; i < repeatSelection; i++) {
            Enchantment enchantment = getRandomEnchantment();
            enchants.merge(enchantment, chanceEnchantmentAndGetLevel(enchantment), Integer::sum);
        }
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (!entry.getKey().canEnchantItem(item)) continue;
            if (entry.getValue() <= 0) continue;
            if (!LifeEnchantment.getEnchantmentDescription(entry.getKey()).isEmpty()) continue;
            meta.addEnchant(entry.getKey(), entry.getValue(), false);
        }
    }

    private void setCustomStoredEnchantment(EnchantmentStorageMeta meta) {
        int repeatSelection = getRandomSelection(0, 5);
        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (int i = 0; i < repeatSelection; i++) {
            Enchantment enchantment = getRandomEnchantment();
            enchants.merge(enchantment, chanceEnchantmentAndGetLevel(enchantment), Integer::sum);
        }
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getValue() <= 0) continue;
            if (!LifeEnchantment.getEnchantmentDescription(entry.getKey()).isEmpty()) continue;
            meta.addStoredEnchant(entry.getKey(), entry.getValue(), false);
        }
    }

    private int getRandomSelection(int min, int max) {
        int repeatSelection = 1;
        double base = 0.75;
        double randomSelection = Math.random();
        for (int i = 0; i < LifeEnchantment.getCustomEnchantments().size(); i++) {
            if (Math.pow(base, i) < randomSelection) {
                repeatSelection++;
            }
        }
        return Math.max(min, Math.min(max, repeatSelection));
    }

    private Enchantment getRandomEnchantment() {
        return new ArrayList<>(LifeEnchantment.getCustomEnchantmentWithWeight())
                .get(LifeNewPvE.RANDOM.nextInt(LifeEnchantment.getCustomEnchantmentWithWeight().size()));
    }

    private int chanceEnchantmentAndGetLevel(Enchantment enchantment) {
        int result = -1;
        double chance = LifeEnchantment.getCustomEnchantmentChanceMap().get(enchantment);
        int boarder = LifeNewPvE.RANDOM.nextInt(100) + 1;
        while (boarder <= chance) {
            if (result == -1) {
                result = 1;
            } else {
                result++;
            }
            chance*= getRandomChance() + 0.3;
        }
        return result;
    }

    private double getRandomChance() {
        return (LifeNewPvE.RANDOM.nextDouble(20) + 1) / 100;
    }
}
