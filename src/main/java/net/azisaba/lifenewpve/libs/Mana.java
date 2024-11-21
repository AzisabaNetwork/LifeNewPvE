package net.azisaba.lifenewpve.libs;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ConstantValue")
public class Mana extends BukkitRunnable {

    private final Player player;

    public Mana(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline()) {
            stop();
        } else {
            modifyMana(player,0.05, ManaModifyEvent.Type.AUTO_REGEN);
        }
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
            return (s == null) ? 1000 + getWearingMana(p) : Long.parseLong(s) + getWearingMana(p);
        } else {
            return 1000 + getWearingMana(p);
        }
    }

    private static void handleManaModification(@NotNull Player player, PersistentDataContainer pc, long set, ManaModifyEvent.Type type) {
        JavaPlugin.getPlugin(LifeNewPvE.class).runSync(() -> {
            ManaModifyEvent event = new ManaModifyEvent(player, getMana(pc), set, type, getMaxMana(player, pc));
            if (!event.callEvent()) return;
            setMana(event.getPlayer(), Math.min(event.getBefore() + event.getAdd(), event.getMax()));
        });
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
        if (item == null || !item.hasItemMeta()) return 0;
        if (item.hasItemMeta()) {
            String mana = item.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "max_mana"), PersistentDataType.STRING);
            if (mana == null) return 0;
            return Long.parseLong(mana);
        }
        return 0;
    }

    public static long getLoreStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return -1;
        if (item.hasItemMeta()) {
            String mana = item.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "max_mana"), PersistentDataType.STRING);
            if (mana == null) return -1;
            return Long.parseLong(mana);
        }
        return -1;
    }
}
