package net.azisaba.lifenewpve.mythicmobs.conditons;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class WeekOfDay implements ISkillCondition, ISkillMetaCondition {

    private final int i;

    public WeekOfDay(@NotNull MythicLineConfig config) {
        this.i = Integer.parseInt(config.getPlaceholderString(new String[]{"day", "d"}, "1").get());
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {
        LocalDateTime now = LocalDateTime.now();
        return now.getDayOfWeek().getValue() == i;
    }
}
