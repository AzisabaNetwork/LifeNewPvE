package net.azisaba.lifenewpve.mana;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.api.mana.IManaRegen;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class ManaRegen extends ManaBase implements IManaRegen {

    private final double MANA_REGEN_BASE = 25;

    public ManaRegen(@NotNull Player player, LifeNewPvE plugin) {
        super(player, plugin);
    }

    private static final Multimap<UUID, BukkitTask> TASKS = HashMultimap.create();

    @Override
    public void startRegen(Runnable runnable, long delay, long period) {
        Mana m = new Mana(player, plugin, runnable, delay, period);
        TASKS.put(player.getUniqueId(), m.getTask());
    }

    @Override
    public void autoRegen() {
        Mana m = new Mana(player, plugin, ()-> ManaUtil.multiplyMana(player, 0.05), 200, 200);
        TASKS.put(player.getUniqueId(), m.getTask());
    }

    @Override
    public double getManaRegen() {
        String string = pc.get(plugin.getKey().getOrCreate("mana_regen"), PersistentDataType.STRING);
        return string == null ? MANA_REGEN_BASE : Double.parseDouble(string) + MANA_REGEN_BASE;
    }

    @Override
    public void setManaRegen(double manaRegen) {
        pc.set(plugin.getKey().getOrCreate("mana_regen"), PersistentDataType.STRING, String.valueOf(manaRegen));
    }

    @Override
    public void addManaRegen(double manaRegen) {
        setManaRegen(getManaRegen() + manaRegen);
    }

    @Override
    public double getManaRegenBase() {
        return MANA_REGEN_BASE;
    }

    @Override
    public void stopRegen() {
        TASKS.get(player.getUniqueId()).forEach(BukkitTask::cancel);
    }

    public static  String getPlaceholder() {
        String string = "%{mana}" + "%{mana_regen}%";
        for (Map.Entry<String, String> entry : getManaPlaceholderMap().entrySet()) {
            string = string.replace("%{" + entry.getKey() + "}", entry.getValue());
        }
        return string;
    }
}
