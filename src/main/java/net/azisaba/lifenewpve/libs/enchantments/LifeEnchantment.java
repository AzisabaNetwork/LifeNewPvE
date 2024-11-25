package net.azisaba.lifenewpve.libs.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;


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
    private static final NamespacedKey ALL_ELEMENT_DAMAGE_KEY = new NamespacedKey("minecraft", "all_element_boost");
    private static final NamespacedKey ALL_DAMAGE_KEY = new NamespacedKey("minecraft", "all_damage_boost");
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

    @Contract(value = " -> new", pure = true)
    public static @Unmodifiable List<Enchantment> getCustomEnchantments() {
        return List.of(MANA_REFINEMENT, MANA_BOOSTER, MANA_REGEN, MANA_STEAL);
    }
}

