package net.azisaba.lifenewpve.libs;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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
            modifyMana(player,0.05);
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

    public static long getMaxMana(@NotNull PersistentDataContainer pc) {
        NamespacedKey maxManaKey = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "max_mana");
        if (pc.has(maxManaKey, PersistentDataType.STRING)) {
            String s = pc.get(maxManaKey, PersistentDataType.STRING);
            return (s == null) ? 1000 : Long.parseLong(s);
        } else {
            return 1000;
        }
    }

    public static void modifyMana(@NotNull Player player, long add) {
        add += getMana(player.getPersistentDataContainer());
        long finalAdd = add;
        JavaPlugin.getPlugin(LifeNewPvE.class).runSync(()-> {
            ManaModifyEvent event = new ManaModifyEvent(player, getMana(player.getPersistentDataContainer()), finalAdd);
            if (!event.callEvent()) return;
            setMana(player, finalAdd);
        });
    }

    public static void modifyMana(@NotNull Player player, double multiple) {
        PersistentDataContainer pc = player.getPersistentDataContainer();
        long add = Math.round(Math.min(multiple * getMaxMana(pc) + getMana(pc), getMaxMana(pc)));
        JavaPlugin.getPlugin(LifeNewPvE.class).runSync(()-> {
            ManaModifyEvent event = new ManaModifyEvent(player, getMana(player.getPersistentDataContainer()), add);
            if (!event.callEvent()) return;
            setMana(player, add);
        });

    }

    public static void setMana(@NotNull Player player, long set) {
        player.getPersistentDataContainer()
                .set(new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "mana"), PersistentDataType.STRING, String.valueOf(set));
    }
}
