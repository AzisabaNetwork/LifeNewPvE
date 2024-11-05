package net.azisaba.lifenewpve.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class addYaw implements ISkillMechanic, ITargetedEntitySkill {

    private final float yaw;

    public addYaw(@NotNull MythicLineConfig config) {
        this.yaw = Float.parseFloat(PlaceholderString.of(config.getString(new String[]{"y", "yaw", "v", "value"}, "0")).get());
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, @NotNull AbstractEntity abstractEntity) {
        Entity entity = abstractEntity.getBukkitEntity();
        if (entity == null) return SkillResult.CONDITION_FAILED;
        try {
            entity.setRotation(fixYaw(entity.getYaw()), entity.getPitch());
        } catch (Exception ignored) {
        }
        return SkillResult.SUCCESS;
    }

    private float fixYaw(float base) {
        return Float.isNaN(base) ? yaw : yaw + base;
    }
}
