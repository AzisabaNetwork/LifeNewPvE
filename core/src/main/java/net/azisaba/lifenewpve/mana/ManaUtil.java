package net.azisaba.lifenewpve.mana;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.enchantments.LifeEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ManaUtil {

    public static void addMana(Player player, double mana) {
        ManaBase base = new ManaBase(player, LifeNewPvE.getInstance());
        base.setMana(Math.min(base.getMana() + mana, base.getMaxMana()));
    }

    public static void multiplyMana(Player player, double multiplier) {
        ManaBase base = new ManaBase(player, LifeNewPvE.getInstance());
        double m = base.getMaxMana() * multiplier;
        base.setMana(Math.min(m + base.getMana(), base.getMaxMana()));
    }

    public static double getMana(Player p) {
        return new ManaBase(p, LifeNewPvE.getInstance()).getMana();
    }

    public static double getMaxMana(Player p) {
        return new ManaBase(p, LifeNewPvE.getInstance()).getMaxMana();
    }

    public static void setMana(Player player, double mana) {
        ManaBase base = new ManaBase(player, LifeNewPvE.getInstance());
        base.setMana(Math.min(mana, base.getMaxMana()));
    }

    public static void setMaxMana(Player player, double mana) {
        new ManaBase(player, LifeNewPvE.getInstance()).setMaxMana(mana);
    }

    public static double getManaSteal(Player player) {
        return new ManaSteal(player, LifeNewPvE.getInstance()).getManaSteal();
    }

    public static String getManaRegenPlaceholder() {
        return ManaRegen.getPlaceholder();
    }

    public static String getManaBoostPlaceholder() {
        return ManaBooster.getPlaceholder();
    }

    public static String getManaStealPlaceholder() {
        return ManaSteal.getPlaceholder();
    }

    public static double getItemMana(Player p, String name) {
        ManaBase base = new ManaBase(p, LifeNewPvE.getInstance());
        Object ob = base.getAllPDC(LifeNewPvE.getInstance().getKey().getOrCreate(name), PersistentDataType.STRING);
        if (ob instanceof Integer i) {
            return i.doubleValue();
        } else {
            return -1;
        }
    }

    public static double getItemMana(Player p, String name, ItemStack item) {
        ManaBase base = new ManaBase(p, LifeNewPvE.getInstance());
        String ob = base.getPDC(item, LifeNewPvE.getInstance().getKey().getOrCreate(name), PersistentDataType.STRING);
        try {
            return Integer.parseInt(ob);
        } catch (NumberFormatException e) {
            return  -1;
        }
    }

    @NotNull
    public static List<Enchantment> getContainsEnchantment(ItemStack item) {
        List<Enchantment> set = new ArrayList<>();
        if (item == null || !item.hasItemMeta() || item.getType() != Material.ENCHANTED_BOOK) return set;
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
        if (!meta.hasStoredEnchants()) return set;
        meta.getStoredEnchants()
                .entrySet()
                .stream()
                .filter(enchantmentIntegerEntry -> !LifeEnchantment.getEnchantmentDescription(enchantmentIntegerEntry.getKey()).isEmpty())
                .forEach(enchantmentIntegerEntry -> set.add(enchantmentIntegerEntry.getKey()));
        return set;
    }
}
