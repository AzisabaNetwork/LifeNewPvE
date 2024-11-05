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

public class Rotate implements ISkillMechanic, ITargetedEntitySkill {

    private final float yaw;

    public Rotate(@NotNull MythicLineConfig config) {
        this.yaw = Float.parseFloat(PlaceholderString.of(config.getString(new String[]{"y", "yaw", "v", "value"}, "0")).get());
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, @NotNull AbstractEntity abstractEntity) {
        Entity entity = abstractEntity.getBukkitEntity();
        entity.setRotation(fixYaw(yaw), entity.getPitch());
        return SkillResult.SUCCESS;
    }

    private float fixYaw(float yaw) {
        if (yaw > 180 || yaw < -180) {
            float fix = yaw % 360;
            if (fix > 180) {
                fix -= 360;
            } else if (fix < -180) {
                fix += 360;
            }
            return fix;
        }
        return yaw;
    }
}
