package net.azisaba.lifenewpve.mana;

import net.azisaba.api.mana.IManaBooster;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ManaBooster extends ManaBase implements IManaBooster {

    private final double MANA_BOOST_BASE = 10;

    public ManaBooster(@NotNull Player player, LifeNewPvE plugin) {
        super(player, plugin);
    }

    public static String getPlaceholder() {
        String string = "%{mana}+ " + "%{mana_booster}";
        for (Map.Entry<String, String> entry : getManaPlaceholderMap().entrySet()) {
            string = string.replace("%{" + entry.getKey() + "}", entry.getValue());
        }
        return string;
    }

    @Override
    public double getManaBoost() {
        String string = pc.get(plugin.getKey().getOrCreate("mana_booster"), PersistentDataType.STRING);
        return string == null ? MANA_BOOST_BASE + ManaUtil.getItemMana(player, "max_mana") : Double.parseDouble(string) + MANA_BOOST_BASE + ManaUtil.getItemMana(player, "max_mana");
    }

    @Override
    public void setManaBoost(double manaBoost) {
        if (manaBoost < 0) manaBoost = 0;
        pc.set(plugin.getKey().getOrCreate("mana_booster"), PersistentDataType.STRING, String.valueOf(manaBoost));
    }

    @Override
    public void addManaBoost(double manaBoost) {
        setManaBoost(getManaBoost() + manaBoost);
    }

    @Override
    public double getManaBoostBase() {
        return MANA_BOOST_BASE;
    }
}
