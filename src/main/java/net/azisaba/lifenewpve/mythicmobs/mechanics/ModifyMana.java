package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifenewpve.libs.Mana;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ModifyMana implements ISkillMechanic, ITargetedEntitySkill {

    private final long add;

    private final double multiply;

    private final boolean multiple;

    public ModifyMana(@NotNull MythicLineConfig config) {
        this.multiply = Double.parseDouble(config.getPlaceholderString(new String[]{"a", "amount"}, "0.01").get());
        this.add = Long.parseLong(config.getPlaceholderString(new String[]{"a", "amount"}, "1").get());
        this.multiple = config.getBoolean(new String[]{"m", "multiple"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (abstractEntity == null) return SkillResult.INVALID_TARGET;
        if (!abstractEntity.isPlayer()) return SkillResult.INVALID_TARGET;
        Player player = BukkitAdapter.adapt(abstractEntity.asPlayer());
        if (multiple) {
            Mana.modifyMana(player, multiply, ManaModifyEvent.Type.CUSTOM);
        } else {
            Mana.modifyMana(player, add, ManaModifyEvent.Type.CUSTOM);
        }
        return SkillResult.SUCCESS;
    }
}
