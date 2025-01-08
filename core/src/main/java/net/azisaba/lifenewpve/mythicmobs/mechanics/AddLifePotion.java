package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.potion.LifePotion;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class AddLifePotion implements ISkillMechanic, ITargetedEntitySkill {

    private final LifeNewPvE plugin;

    private final int level;

    private final long seconds;

    private final String name;

    public AddLifePotion(LifeNewPvE plugin, @NotNull MythicLineConfig config) {
        this.plugin = plugin;
        this.name = config.getString(new String[]{"name", "n", "t", "type"}, "life");
        this.level = config.getInteger(new String[]{"level", "l", "a", "amount"}, 1);
        this.seconds = config.getLong(new String[]{"seconds", "s", "d", "duration"}, 30);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, @NotNull AbstractEntity abstractEntity) {
        if (!(BukkitAdapter.adapt(abstractEntity) instanceof LivingEntity living)) return SkillResult.CONDITION_FAILED;
        LifePotion potion = new LifePotion(plugin, living);
        potion.addPotion(name, level, seconds);
        potion.init();
        return SkillResult.SUCCESS;
    }
}
