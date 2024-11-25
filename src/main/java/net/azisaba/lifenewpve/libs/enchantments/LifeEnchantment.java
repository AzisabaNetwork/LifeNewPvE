package net.azisaba.lifenewpve.libs.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LifeEnchantment {

    public static Enchantment MANA_REFINEMENT;
    public static Enchantment MANA_BOOSTER;
    public static Enchantment MANA_REGEN;
    public static Enchantment MANA_STEAL;
    public static Enchantment ALL_ELEMENT_DAMAGE;
    public static Enchantment ALL_DAMAGE;
    public static Enchantment ALL_DEFENCE;

    private static final NamespacedKey MANA_REFINEMENT_KEY = new NamespacedKey("minecraft", "mana_refinement");
    private static final NamespacedKey MANA_BOOSTER_KEY = new NamespacedKey("minecraft", "mana_boost");
    private static final NamespacedKey MANA_REGEN_KEY = new NamespacedKey("minecraft", "mana_regen");
    private static final NamespacedKey MANA_STEAL_KEY = new NamespacedKey("minecraft", "mana_steal");
    private static final NamespacedKey ALL_ELEMENT_DAMAGE_KEY = new NamespacedKey("minecraft", "all_element_damage");
    private static final NamespacedKey ALL_DAMAGE_KEY = new NamespacedKey("minecraft", "all_damage");
    private static final NamespacedKey ALL_DEFENCE_KEY = new NamespacedKey("minecraft", "all_defence");

    static {
        initializeEnchantments();
    }

    private static void initializeEnchantments() {
        for (Enchantment enchantment : Enchantment.values()) {
            NamespacedKey key = enchantment.getKey();
            if (key.equals(MANA_REFINEMENT_KEY)) {
                MANA_REFINEMENT = enchantment;
            } else if (key.equals(MANA_BOOSTER_KEY)) {
                MANA_BOOSTER = enchantment;
            } else if (key.equals(MANA_REGEN_KEY)) {
                MANA_REGEN = enchantment;
            } else if (key.equals(MANA_STEAL_KEY)) {
                MANA_STEAL = enchantment;
            } else if (key.equals(ALL_ELEMENT_DAMAGE_KEY)) {
                ALL_ELEMENT_DAMAGE = enchantment;
            } else if (key.equals(ALL_DAMAGE_KEY)) {
                ALL_DAMAGE = enchantment;
            } else if (key.equals(ALL_DEFENCE_KEY)) {
                ALL_DEFENCE = enchantment;
            }

        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static List<Enchantment> getCustomEnchantments() {
        return new ArrayList<>(List.of(MANA_REFINEMENT, MANA_BOOSTER, MANA_REGEN, MANA_STEAL, ALL_ELEMENT_DAMAGE, ALL_DAMAGE, ALL_DEFENCE));
    }

    @NotNull
    public static List<Enchantment> getCustomEnchantmentWithWeight() {
        List<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment enchantment : getCustomEnchantments()) {
            for (int i = 0; i < enchantment.getWeight(); i++) {
                enchantments.add(enchantment);
            }
        }
        return enchantments;
    }

    @NotNull
    public static Map<Enchantment, Integer> getCustomEnchantmentChanceMap() {
        Map<Enchantment, Integer> enchantmentIntegerMap = new HashMap<>();
        for (Enchantment enchantment : getCustomEnchantments()) {
            if (enchantment.equals(ALL_ELEMENT_DAMAGE)) enchantmentIntegerMap.put(enchantment, 80);
            if (enchantment.equals(ALL_DAMAGE)) enchantmentIntegerMap.put(enchantment, 75);
            if (enchantment.equals(ALL_DEFENCE)) enchantmentIntegerMap.put(enchantment, 65);
            if (enchantment.equals(MANA_REFINEMENT)) enchantmentIntegerMap.put(enchantment, 95);
            if (enchantment.equals(MANA_BOOSTER)) enchantmentIntegerMap.put(enchantment, 85);
            if (enchantment.equals(MANA_REGEN)) enchantmentIntegerMap.put(enchantment, 70);
            if (enchantment.equals(MANA_STEAL)) enchantmentIntegerMap.put(enchantment, 60);
        }
        return enchantmentIntegerMap;
    }
}

