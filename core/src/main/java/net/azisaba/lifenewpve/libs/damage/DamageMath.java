package net.azisaba.lifenewpve.libs.damage;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifenewpve.libs.enchantments.LifeEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DamageMath {

    private static final double player_defence = 20.0;

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

    @SuppressWarnings("unused")
    public double getPlayerDefence() {
        return player_defence;
    }

    @SuppressWarnings("unused")
    public double getLevelDefence() {
        return defence_amount_per_add;
    }

    @SuppressWarnings("unused")
    public double getElementMultiplier() {
        return damage_multiplier;
    }

    public static double getCalculatedDamage(double damage, double a, double t, @NotNull AbstractEntity victim, @NotNull AbstractEntity attacker, String element) {
        if (attacker.isPlayer()) {
            Player atk = BukkitAdapter.adapt(attacker.asPlayer());
            if (element != null) {
                damage *= getElementDamage(atk);
            }
            damage *= getAllDamage(atk);
        }

        if (!victim.isPlayer()) {
            return calculateMob(damage, a, t);
        } else {
            Player dmg = BukkitAdapter.adapt(victim.asPlayer());
            double offset = player_defence + getProtection(dmg);
            return calculate(damage, a, t, offset);
        }
    }

    private static double calculateMob(double damage, double a, double t) {
        if (damage <= 0) return 0;
        double armor = 2 * a + t;
        double math = damage * getArmorScaleCut(armor, a);
        return Double.isInfinite(math) || Double.isNaN(math) ? damage : math;
    }

    private static double calculate(double damage, double a, double t, double offset) {
        if (damage <= 0) return 0;
        double armor = 2 * a + t + offset;
        damage = getArmorRewardCut(damage, armor, t);

        double math = getDamageScaleCut(damage, armor) * getArmorScaleCut(armor, a);
        return Double.isInfinite(math) || Double.isNaN(math) ? damage : math;
    }

    private static double getDamageScaleCut(double damage, double armor) {
        double f = 1 + damage;
        return  Math.max(damage / (armor + f) * f, 0);
    }

    private static double getArmorScaleCut(double armor, double a) {
        double m = armor * 2 - a;
        return  Math.pow(m / (m + armor), 2);
    }

    private static double getArmorRewardCut(double damage, double armor, double t) {
        if (armor < 0) {
            return damage * Math.pow(1.025, Math.abs(armor));
        } else {
            damage *= Math.pow(0.995, Math.abs(t));
        }
        return damage;
    }

    protected static double getProtection(@NotNull Player p) {
       double protection = 0;
       for (ItemStack i : getItemInventory(p)) {
           protection += getEnchantmentInSlot(i, LifeEnchantment.ALL_DEFENCE, defence_amount_per_add, 0);
       }
       return protection;
    }

    protected static double getElementDamage(@NotNull Player p) {
        double element = 1;
        for (ItemStack i : getItemInventory(p)) {
            element *= getEnchantmentInSlot(i, LifeEnchantment.ALL_ELEMENT_DAMAGE, damage_multiplier, 1);
        }
        return element;
    }

    protected static double getAllDamage(@NotNull Player p) {
        double all = 1;
        for (ItemStack i : getItemInventory(p)) {
            all *= getEnchantmentInSlot(i, LifeEnchantment.ALL_DAMAGE, damage_multiplier, 1);
        }
        return all;
    }

    @NotNull
    protected static Set<ItemStack> getItemInventory(@NotNull Player p) {
        Set<ItemStack> set = new HashSet<>(Arrays.stream(p.getInventory().getArmorContents()).toList());
        set.add(p.getInventory().getItemInMainHand());
        return set;
    }

    protected static double getEnchantmentInSlot(ItemStack i, Enchantment select, double multiplier, int result) {
        if (i == null || !i.hasItemMeta() || select == null || !i.getItemMeta().hasEnchant(select)) return result;
        return (i.getItemMeta().getEnchantLevel(select) * multiplier) + result;
    }
}
