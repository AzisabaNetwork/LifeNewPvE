package net.azisaba.lifenewpve.libs.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class LifeEnchantment {

    public static final Enchantment MANA_REFINEMENT = Enchantment.getByKey(new NamespacedKey("minecraft", "mana_refinement"));

    public static final Enchantment MANA_BOOSTER = Enchantment.getByKey(new NamespacedKey("minecraft", "mana_boost"));

    public static final Enchantment MANA_REGEN = Enchantment.getByKey(new NamespacedKey("minecraft", "mana_regen"));

    public static final Enchantment MANA_STEAL = Enchantment.getByKey(new NamespacedKey("minecraft", "mana_steal"));

    @Contract(value = " -> new", pure = true)
    public static @Unmodifiable List<Enchantment> getCustomEnchantments() {
        if (MANA_REFINEMENT == null || MANA_BOOSTER == null || MANA_REGEN == null || MANA_STEAL == null) {
            return List.of();
        } else {
            return List.of(MANA_REFINEMENT, MANA_BOOSTER, MANA_REGEN, MANA_STEAL);
        }
    }
}
