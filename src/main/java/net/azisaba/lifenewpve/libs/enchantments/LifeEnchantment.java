package net.azisaba.lifenewpve.libs.enchantments;

import net.azisaba.lifenewpve.libs.damage.DamageMath;
import net.azisaba.lifenewpve.libs.mana.Mana;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;


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

    private static final Map<Enchantment, List<String>> ENCHANTMENT_MAP;

    static {
        ENCHANTMENT_MAP = new HashMap<>();
        initializeEnchantments();
    }


    private static void initializeEnchantments() {
        for (Enchantment enchantment : Enchantment.values()) {
            NamespacedKey key = enchantment.getKey();
            if (key.equals(MANA_REFINEMENT_KEY)) {
                MANA_REFINEMENT = enchantment;
                ENCHANTMENT_MAP.put(
                        MANA_REFINEMENT,
                        List.of(
                                "§f- §fレベルごとに§d" + Mana.getManaRefinementString() + "§fマナの",
                                "  §f消費量・獲得量を効率良くする。"
                        ));
            } else if (key.equals(MANA_BOOSTER_KEY)) {
                MANA_BOOSTER = enchantment;
                ENCHANTMENT_MAP.put(
                        MANA_BOOSTER,
                        List.of(
                                "§f- §fレベルごとに最大マナ量を",
                                "  §d" + Mana.getManaBoosterString() + "§f増加させる。"
                        ));
            } else if (key.equals(MANA_REGEN_KEY)) {
                MANA_REGEN = enchantment;
                ENCHANTMENT_MAP.put(
                        MANA_REGEN,
                        List.of(
                                "§f- §fレベルごとに時間経過回復の",
                                "  §fマナを§d" + Mana.getManaRegenString() + "§f増加させる。"
                        ));
            } else if (key.equals(MANA_STEAL_KEY)) {
                MANA_STEAL = enchantment;
                ENCHANTMENT_MAP.put(
                        MANA_STEAL,
                        List.of(
                                "§f- §fMob討伐時に10%で発動。",
                                "  §fレベルごとに§d" + Mana.getManaStealString() + "§fマナを回復する。"
                        ));
            } else if (key.equals(ALL_ELEMENT_DAMAGE_KEY)) {
                ALL_ELEMENT_DAMAGE = enchantment;
                ENCHANTMENT_MAP.put(
                        ALL_ELEMENT_DAMAGE,
                        List.of(
                                "§f- §f属性が存在する攻撃のダメージを",
                                "  §fレベルごとに§c" + DamageMath.getDamageString() + "§f増加する。"
                        ));
            } else if (key.equals(ALL_DAMAGE_KEY)) {
                ALL_DAMAGE = enchantment;
                ENCHANTMENT_MAP.put(
                        ALL_DAMAGE,
                        List.of(
                                "§f- §f全ての攻撃のダメージを",
                                "  §fレベルごとに§c" + DamageMath.getDamageString() + "§f増加する。"
                        ));
            } else if (key.equals(ALL_DEFENCE_KEY)) {
                ALL_DEFENCE = enchantment;
                ENCHANTMENT_MAP.put(
                        ALL_DEFENCE,
                        List.of(
                                "§f- §fレベルごとに§9" + DamageMath.getDefenceString() + "§f防御を増加する。"
                        ));
            }
        }
    }

    public static List<String> getEnchantmentDescription(Enchantment enchantment) {
        if (!ENCHANTMENT_MAP.containsKey(enchantment)) return new ArrayList<>();
        return ENCHANTMENT_MAP.get(enchantment);
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
            if (enchantment.equals(ALL_ELEMENT_DAMAGE)) enchantmentIntegerMap.put(enchantment, 75);
            if (enchantment.equals(ALL_DAMAGE)) enchantmentIntegerMap.put(enchantment,60);
            if (enchantment.equals(ALL_DEFENCE)) enchantmentIntegerMap.put(enchantment, 50);
            if (enchantment.equals(MANA_REFINEMENT)) enchantmentIntegerMap.put(enchantment, 80);
            if (enchantment.equals(MANA_BOOSTER)) enchantmentIntegerMap.put(enchantment, 70);
            if (enchantment.equals(MANA_REGEN)) enchantmentIntegerMap.put(enchantment, 55);
            if (enchantment.equals(MANA_STEAL)) enchantmentIntegerMap.put(enchantment, 45);
        }
        return enchantmentIntegerMap;
    }
}

