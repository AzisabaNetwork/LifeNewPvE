package net.azisaba.lifenewpve.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RaidBoss implements ISkillMechanic, ITargetedEntitySkill {

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, @NotNull AbstractEntity abstractEntity) {
        if (abstractEntity.isPlayer()) return SkillResult.CONDITION_FAILED;
        if (!abstractEntity.isLiving()) return SkillResult.CONDITION_FAILED;

        NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "raid_boss");
        abstractEntity.getDataContainer().set(key, PersistentDataType.STRING, "true");
        return SkillResult.SUCCESS;
    }
}
