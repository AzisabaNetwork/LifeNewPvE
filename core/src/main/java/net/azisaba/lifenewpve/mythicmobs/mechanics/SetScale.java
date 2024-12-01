package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.utils.Schedulers;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class SetScale implements ISkillMechanic, ITargetedEntitySkill {

    private final float scale;

    private final long time;

    public SetScale(@NotNull MythicLineConfig config) {
        this.scale = Float.parseFloat(config.getPlaceholderString(new String[]{"s", "scale", "size"}, "1").get());
        this.time = Long.parseLong(config.getPlaceholderString(new String[]{"t", "time", "d", "duration"}, "1").get());
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (abstractEntity == null) return null;
        if (abstractEntity.isPlayer()) return SkillResult.CONDITION_FAILED;

        if (abstractEntity.getBukkitEntity() instanceof LivingEntity living) {
            livingScale(living);
            return SkillResult.SUCCESS;

        } else if (abstractEntity.getBukkitEntity() instanceof Display display) {
            displayScale(display);
            return SkillResult.SUCCESS;
        }
        return SkillResult.CONDITION_FAILED;
    }

    private void livingScale(@NotNull LivingEntity living) {
        float s = scale / time;
        AttributeInstance attr = living.getAttribute(Attribute.GENERIC_SCALE);
        if (attr == null) return;
        livingTask(attr, s, new AtomicInteger(0));
    }

    private void livingTask(@NotNull AttributeInstance attr, float s, @NotNull AtomicInteger count) {
        if (count.get() >= time) return;

        NamespacedKey key = new NamespacedKey("lifenewpve", "scale" + count.get());
        attr.addModifier(new AttributeModifier(key, s, AttributeModifier.Operation.ADD_NUMBER));

        Schedulers.async().runLater(()-> {
            count.set(count.get() + 1);
            livingTask(attr, s, count);
        }, 1);
    }

    private void displayScale(@NotNull Display display) {
        float s = scale / time;
        displayTask(display, s, new AtomicInteger(0));
    }

    private void displayTask(Display display, float s, @NotNull AtomicInteger count) {
        if (count.get() >= time) return;

        Transformation t = display.getTransformation();
        t.getScale().add(s, s, s);
        display.setTransformation(t);

        Schedulers.async().runLater(()-> {
            count.set(count.get() + 1);
            displayTask(display, s, count);
        }, 1);
    }
}
