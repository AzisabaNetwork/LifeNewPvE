package net.azisaba.lifenewpve.libs.mana;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.enchantments.LifeEnchantment;
import net.azisaba.lifenewpve.libs.event.ManaModifiedEvent;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("ConstantValue")
public class Mana extends BukkitRunnable {

    private final Player player;

    public Mana(@NotNull Player player) {
        this.player = player;
    }

    private static final double MANA_REGEN_VALUE = 0.1;

    private static final double MANA_REFINEMENT_VALUE = 0.01;

    private static final long MANA_BOOSTER_VALUE = 10;

    private static final double MANA_STEAL_VALUE = 0.01;

    @Override
    public void run() {
        if (player == null || !player.isOnline()) {
            stop();
        } else {
            modifyMana(player, getManaRegen(player), ManaModifyEvent.Type.AUTO_REGEN);
        }
    }

    private double getManaRegen(@NotNull Player p) {
        AtomicReference<Double> refinement = new AtomicReference<>(1D);
        Arrays.stream(p.getInventory().getArmorContents()).filter(i -> {
            Enchantment get = LifeEnchantment.MANA_REGEN;
            return  (i != null && i.hasItemMeta() && get != null && i.getItemMeta().hasEnchant(get));
        }).forEach(i -> refinement.updateAndGet(v -> v +  i.getEnchantmentLevel(LifeEnchantment.MANA_REGEN) * MANA_REGEN_VALUE));

        return 0.05 * refinement.get();
    }

    public void stop() {
        cancel();
    }

    public static long getMana(@NotNull PersistentDataContainer pc) {
        NamespacedKey manaKey = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "mana");
        if (pc.has(manaKey, PersistentDataType.STRING)) {
            String s = pc.get(manaKey, PersistentDataType.STRING);
            return (s == null) ? 0 : Long.parseLong(s);
        } else {
            return 0;
        }
    }

    public static long getMaxMana(Player p, @NotNull PersistentDataContainer pc) {
        NamespacedKey maxManaKey = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "max_mana");
        if (pc.has(maxManaKey, PersistentDataType.STRING)) {
            String s = pc.get(maxManaKey, PersistentDataType.STRING);
            return (s == null) ? 500 + getWearingMana(p) : Long.parseLong(s) + getWearingMana(p);
        } else {
            return 500 + getWearingMana(p);
        }
    }

    private static void handleManaModification(@NotNull Player player, PersistentDataContainer pc, long set, ManaModifyEvent.Type type) {
        long modified = Math.round(getManaRefinement(player, set));
        JavaPlugin.getPlugin(LifeNewPvE.class).runSync(() -> {
            ManaModifyEvent event = new ManaModifyEvent(player, getMana(pc), modified, type, getMaxMana(player, pc));
            if (!event.callEvent()) return;
            setMana(event.getPlayer(), Math.min(event.getBefore() + event.getAdd(), event.getMax()));
        });
    }

    private static double getManaRefinement(@NotNull Player p, long modify) {
        AtomicReference<Double> refinement = new AtomicReference<>(1D);
        Arrays.stream(p.getInventory().getArmorContents()).filter(i -> {
            Enchantment get = LifeEnchantment.MANA_REFINEMENT;
            return  (i != null && i.hasItemMeta() && get != null && i.getItemMeta().hasEnchant(get));
        }).forEach(i -> refinement.updateAndGet(v -> v +  i.getEnchantmentLevel(LifeEnchantment.MANA_REFINEMENT) * MANA_REFINEMENT_VALUE));

        if (modify < 0) {
            return modify / refinement.get();
        } else {
            return modify * refinement.get();
        }
    }

    public static void modifyMana(@NotNull Player player, long add, ManaModifyEvent.Type type) {
        PersistentDataContainer pc = player.getPersistentDataContainer();
        handleManaModification(player, pc, add, type);
    }

    public static void modifyMana(@NotNull Player player, double multiple, ManaModifyEvent.Type type) {
        PersistentDataContainer pc = player.getPersistentDataContainer();
        handleManaModification(player, pc, Math.round(multiple * getMaxMana(player, pc)), type);
    }

    public static void setMana(@NotNull Player player, long set) {
        player.getPersistentDataContainer()
                .set(new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "mana"), PersistentDataType.STRING, String.valueOf(set));
        new ManaModifiedEvent(player, set).callEvent();
    }

    public static void setMaxMana(@NotNull Player player, long set) {
        player.getPersistentDataContainer()
                .set(new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "max_mana"), PersistentDataType.STRING, String.valueOf(set));
    }

    private static long getWearingMana(@NotNull Player p) {
        PlayerInventory inv = p.getInventory();
        long mana = 0;
        mana+= getItemMana(inv.getHelmet());
        mana+= getItemMana(inv.getChestplate());
        mana+= getItemMana(inv.getLeggings());
        mana+= getItemMana(inv.getBoots());
        mana+= getItemMana(inv.getItemInMainHand());
        mana+= getItemMana(inv.getItemInOffHand());

        return mana;
    }

    private static long getItemMana(ItemStack item) {
        return getManaFromItemMeta(item);
    }

    public static long getLoreStack(ItemStack item) {
        long mana = getManaFromItemMeta(item);
        return mana > 0 ? mana : -1;
    }

    private static long getManaFromItemMeta(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        long mana = 0;
        String manaS = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey("minecraft", "max_mana"), PersistentDataType.STRING);
        if (manaS != null) {
            mana = Long.parseLong(manaS);
        }
        Enchantment manaBooster = LifeEnchantment.MANA_BOOSTER;
        if (manaBooster != null && item.getItemMeta().hasEnchant(manaBooster)) {
            mana += item.getItemMeta().getEnchantLevel(manaBooster) * MANA_BOOSTER_VALUE;
        }
        return mana;
    }

    public static double getManaSteal(@NotNull Player p) {
        ItemStack i = p.getInventory().getItemInMainHand();
        if (i != null && i.hasItemMeta() && LifeEnchantment.MANA_STEAL != null && i.getItemMeta().hasEnchant(LifeEnchantment.MANA_STEAL)) {
            return i.getItemMeta().getEnchantLevel(LifeEnchantment.MANA_STEAL) * MANA_STEAL_VALUE;
        }
        return 0;
    }

    @NotNull
    public static List<Enchantment> getCustomEnchantments(ItemStack item) {
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

    @NotNull
    public static String getManaRegenString() {
        NumberFormat num = NumberFormat.getNumberInstance();
        num.setMaximumFractionDigits(1);
        return num.format(MANA_REGEN_VALUE * 100) + "%";
    }

    @NotNull
    public static String getManaRefinementString() {
        NumberFormat num = NumberFormat.getNumberInstance();
        num.setMaximumFractionDigits(1);
        return num.format(MANA_REFINEMENT_VALUE * 100) + "%";
    }

    @NotNull
    @Contract(pure = true)
    public static String getManaBoosterString() {
        return "+" + MANA_BOOSTER_VALUE;
    }

    @NotNull
    public static String getManaStealString() {
        NumberFormat num = NumberFormat.getNumberInstance();
        num.setMaximumFractionDigits(1);
        return num.format(MANA_STEAL_VALUE * 100) + "%";
    }
}
