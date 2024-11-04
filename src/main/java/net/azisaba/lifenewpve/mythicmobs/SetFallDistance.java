package net.azisaba.lifenewpve.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.jetbrains.annotations.NotNull;

public class SetFallDistance implements ISkillMechanic, ITargetedEntitySkill {

    private final float fallDistance;

    public SetFallDistance(@NotNull MythicLineConfig config) {
        this.fallDistance = Float.parseFloat(PlaceholderString.of(config.getString(new String[]{"v", "value", "a", "amount"}, "-1")).get());
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (fallDistance == -1) return SkillResult.CONDITION_FAILED;
        abstractEntity.getBukkitEntity().setFallDistance(fallDistance);
        return SkillResult.SUCCESS;
    }
}
