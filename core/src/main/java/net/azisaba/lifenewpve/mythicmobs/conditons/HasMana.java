package net.azisaba.lifenewpve.mythicmobs.conditons;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.numbers.RangedLong;
import net.azisaba.lifenewpve.mana.ManaUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HasMana implements ISkillCondition, IEntityCondition {

    private final RangedLong amount;

    public HasMana(@NotNull MythicLineConfig config) {
        this.amount = new RangedLong(config.getPlaceholderString(new String[]{"a", "amount"}, "10").get());
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        if (abstractEntity == null) return false;
        if (!abstractEntity.isPlayer()) return false;
        AbstractPlayer player = abstractEntity.asPlayer();
        Player p = BukkitAdapter.adapt(player);
        return amount.equals(ManaUtil.getMana(p));
    }
}
