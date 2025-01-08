package net.azisaba.lifenewpve.libs.damage;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.potion.LifePotion;
import net.azisaba.lifenewpve.listeners.potion.PotionEffectListener;
import net.azisaba.lifenewpve.utils.key.LifeKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class DamageMath {

    private static final long defence_amount_per_add = 5;

    private static final double damage_multiplier = 0.05;

    @NotNull
    public static String getDamageString() {
        NumberFormat num = NumberFormat.getNumberInstance();
        num.setMaximumFractionDigits(1);
        return num.format(damage_multiplier * 100) + "%";
    }

    @NotNull
    @Contract(pure = true)
    public static String getDefenceString() {
        return "+" + defence_amount_per_add;
    }

    public static double getCalculatedDamage(double damage, double a, double t, @NotNull AbstractEntity victim, @NotNull AbstractEntity attacker, String element, ItemStack item, boolean isCritical) {
        return damage
                * getWeaponMath(item)
                * getATKMath(attacker, a, t)
                * getPotionMath((LivingEntity) BukkitAdapter.adapt(attacker), (LivingEntity) BukkitAdapter.adapt(victim), element)
                * getCriticalMath((LivingEntity) BukkitAdapter.adapt(attacker), isCritical);
    }

    private static double getATKMath(@NotNull AbstractEntity attacker, double defence, double toughness) {
        double atkDamage = attacker.getDamage();
        return 1 + (atkDamage - defence * (1 + toughness / 100)) / 100;
    }

    private static double getWeaponLevel(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        NamespacedKey key = new LifeKey(LifeNewPvE.getInstance()).getOrCreate("weapon_level");
        if (!meta.getPersistentDataContainer().has(key)) return 0;
        String s = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (s == null) return 0;
        return Double.parseDouble(s);
    }

    private static double getWeaponMath(ItemStack item) {
        return 1 + getWeaponLevel(item) / 100;
    }

    private static double getPotionMath(@NotNull LivingEntity attacker, @NotNull LivingEntity victim, String element) {
        return 1 + (getPotionBuff(attacker, element) + getPotionDebuff(victim, element));
    }

    private static double getCriticalMath(LivingEntity attacker, boolean isCritical) {
        if (isCritical) {
            return 1 + PotionEffectListener.getPotionEffectLevel(attacker, "critical_damage");
        } else {
            return 1+ PotionEffectListener.getPotionEffectLevel(attacker, "no_critical_damage");
        }
    }

    @NotNull
    protected static Map<String, Integer> getPotion(@NotNull LivingEntity living) {
        LifePotion potion = new LifePotion(LifeNewPvE.getInstance(), living);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; potion.getPotionsData().size() > i; i++) {
           String raw = potion.getPotionsData().get(i);

           String[] split = raw.split(":");
           String type = split[0];
           int level = Integer.parseInt(split[1]);
           map.merge(type, level, Integer::sum);
        }
        return map;
    }

    protected static double getPotionBuff(@NotNull LivingEntity living, String element) {
        Map<String, Integer> maps = getPotion(living);
        if (maps.isEmpty()) return 1;
        return maps.getOrDefault("+" + element, 1);
    }

    protected static double getPotionDebuff(@NotNull LivingEntity living, String element) {
        Map<String, Integer> maps = getPotion(living);
        if (maps.isEmpty()) return 1;
        return maps.getOrDefault("-" + element, 1);
    }
}
