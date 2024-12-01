package net.azisaba.lifenewpve.mythicmobs.conditons;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.numbers.RangedInt;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.jetbrains.annotations.NotNull;

public class MythicInRadius implements ISkillCondition, ILocationCondition {

    private final double radius;
    private final RangedInt amount;

    public MythicInRadius(@NotNull MythicLineConfig config) {
        String s = config.getString(new String[]{"a", "amount"}, "1");

        this.radius = config.getPlaceholderDouble(new String[]{"r", "radius"}, "4.0").get();
        this.amount = new RangedInt(s);
    }

    @Override
    public boolean check(@NotNull AbstractLocation abstractLocation) {
        int count = 0;
        for (AbstractEntity ab : MythicBukkit.inst().getVolatileCodeHandler().getWorldHandler().getEntitiesNearLocation(abstractLocation, radius).stream().toList()) {
            ActiveMob mob;
            try {
                mob = MythicBukkit.inst().getMobManager().getActiveMob(ab.getUniqueId()).orElse(null);
                if (mob == null) continue;
            } catch (Exception ignored) {
                continue;
            }
            if (mob.getType().getInternalName() != null) count++;
        }
        return amount.equals(count);
    }
}
