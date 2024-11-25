package net.azisaba.lifenewpve.listeners;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.enchantments.LifeEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EnchantListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EnchantListener.Item(), lifeNewPvE);
    }


    public static class Item extends EnchantListener {


        private record EnchantmentUpgrade(int maxLevel, double upgradeProbability) {}

        @NotNull
        @Contract(" -> new")
        private static Object[][] getEnchantmentUpGrades() {
            return new Object[][]{
                    {Enchantment.SHARPNESS, new EnchantmentUpgrade(20, 0.8)},
                    {Enchantment.UNBREAKING, new EnchantmentUpgrade(10, 0.7)},
                    {Enchantment.SMITE, new EnchantmentUpgrade(10, 0.6)},
                    {Enchantment.SWEEPING_EDGE, new EnchantmentUpgrade(5, 0.5)},
                    {Enchantment.POWER, new EnchantmentUpgrade(10, 0.5)},
                    {Enchantment.IMPALING, new EnchantmentUpgrade(10, 0.5)},
                    {Enchantment.RESPIRATION, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.LOOTING, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.BANE_OF_ARTHROPODS, new EnchantmentUpgrade(10, 0.6)},
                    {Enchantment.LURE, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.EFFICIENCY, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.LUCK_OF_THE_SEA, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.THORNS, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.LOYALTY, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.RIPTIDE, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.FIRE_ASPECT, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.KNOCKBACK, new EnchantmentUpgrade(5, 0.4)},
                    {Enchantment.FLAME, new EnchantmentUpgrade(5, 0.4)},
            };
        }

        private static final Map<Enchantment, EnchantmentUpgrade> ENCHANTMENT_UPGRADES = new HashMap<>();

        static {
            for (Object[] enchantmentUpgrade : getEnchantmentUpGrades()) {
                ENCHANTMENT_UPGRADES.put((Enchantment) enchantmentUpgrade[0], (EnchantmentUpgrade) enchantmentUpgrade[1]);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onItem(@NotNull EnchantItemEvent event) {
            Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();

            enchantsToAdd.keySet().stream()
                    .filter(this::canUpgradeEnchantment)
                    .forEach(enchantment -> upgradeEnchantmentIfPossible(enchantsToAdd, enchantment));
            ItemStack item = event.getItem();
            ItemMeta meta = item.getItemMeta();
            for (Map.Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
            setCustomEnchantment(meta);
            item.setItemMeta(meta);
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

        private void setCustomEnchantment(ItemMeta meta) {

            int[][] chances = {
                    {75, 50, 30},
                    {85, 65, 40, 15, 3},
                    {30, 15, 5},
                    {30, 15, 5}
            };

            for (int i = 0; i < LifeEnchantment.getCustomEnchantments().size(); i++) {
                Enchantment enchantment = new ArrayList<>(LifeEnchantment.getCustomEnchantments()).get(i);
                if (enchantment != null) {
                    int level = calculateEnchantmentLevel(chances[i]);
                    if (level > 0) {
                        meta.addEnchant(enchantment, level, false);
                    }
                }
            }
        }

        private int calculateEnchantmentLevel(@NotNull int[] thresholds) {
            int level = 0;
            int chance = LifeNewPvE.RANDOM.nextInt(100);
            for (int threshold : thresholds) {
                if (chance < threshold) {
                    level++;
                }
            }
            return level;
        }
    }
}
