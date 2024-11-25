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
            setCustomEnchantment(meta, item);
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

        private void setCustomEnchantment(ItemMeta meta, ItemStack item) {
            int repeatSelection = getRandomSelection();
            Map<Enchantment, Integer> enchants = new HashMap<>();
            for (int i = 0; i < repeatSelection; i++) {
                Enchantment enchantment = getRandomEnchantment();
                enchants.merge(enchantment, chanceEnchantmentAndGetLevel(enchantment), Integer::sum);
            }
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                if (!entry.getKey().canEnchantItem(item)) continue;
                if (entry.getValue() <= 0) continue;
                meta.addEnchant(entry.getKey(), entry.getValue(), false);
            }
        }

        private int getRandomSelection() {
            int min = 1;
            int max = 8;
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
}
