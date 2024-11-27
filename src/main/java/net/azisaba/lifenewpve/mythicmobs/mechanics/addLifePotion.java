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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class addLifePotion implements ISkillMechanic, ITargetedEntitySkill {

    private final int level;

    private final long seconds;

    private final String name;

    public addLifePotion(@NotNull MythicLineConfig config) {
        this.name = config.getString(new String[]{"name", "n", "t", "type"}, "life");
        this.level = config.getInteger(new String[]{"level", "l", "a", "amount"}, 1);
        this.seconds = config.getLong(new String[]{"seconds", "s", "d", "duration"}, 30);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, @NotNull AbstractEntity abstractEntity) {
        if (!abstractEntity.isPlayer()) return SkillResult.CONDITION_FAILED;
        Player p = BukkitAdapter.adapt(abstractEntity.asPlayer());
        new LifePotion(JavaPlugin.getPlugin(LifeNewPvE.class), p).addPotion(name, level, seconds);
        return SkillResult.SUCCESS;
    }
}
