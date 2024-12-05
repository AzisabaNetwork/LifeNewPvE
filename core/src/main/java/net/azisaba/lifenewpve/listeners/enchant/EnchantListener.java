package net.azisaba.lifenewpve.listeners.enchant;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EnchantListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EnchantItemListener(lifeNewPvE), lifeNewPvE);
    }

    public record EnchantmentUpgrade(int maxLevel, double upgradeProbability) {}

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

    public static final Map<Enchantment, EnchantmentUpgrade> ENCHANTMENT_UPGRADES = new HashMap<>();

    static {
        for (Object[] enchantmentUpgrade : getEnchantmentUpGrades()) {
            ENCHANTMENT_UPGRADES.put((Enchantment) enchantmentUpgrade[0], (EnchantmentUpgrade) enchantmentUpgrade[1]);
        }
    }
}
