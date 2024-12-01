package net.azisaba.lifenewpve.mana;

import net.azisaba.api.mana.IManaSteal;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ManaSteal extends ManaBase implements IManaSteal {

    private final double MANA_STEAL_BASE = 0;

    public ManaSteal(@NotNull Player player, LifeNewPvE plugin) {
        super(player, plugin);
    }

    public static String getPlaceholder() {
        String string = "%{mana}" + "%{mana_steal}%";
        for (Map.Entry<String, String> entry : getManaPlaceholderMap().entrySet()) {
            string = string.replace("%{" + entry.getKey() + "}", entry.getValue());
        }
        return string;
    }

    @Override
    public double getManaSteal() {
        String string = pc.get(plugin.getKey().getOrCreate("mana_steal"), PersistentDataType.STRING);
        return string == null ? MANA_STEAL_BASE : Double.parseDouble(string) + MANA_STEAL_BASE;
    }

    @Override
    public void setManaSteal(double manaSteal) {
        pc.set(plugin.getKey().getOrCreate("mana_steal"), PersistentDataType.STRING, String.valueOf(manaSteal));
    }

    @Override
    public void addManaSteal(double manaSteal) {
        setManaSteal(getManaSteal() + manaSteal);
    }

    @Override
    public double getManaStealBase() {
        return MANA_STEAL_BASE;
    }
}
