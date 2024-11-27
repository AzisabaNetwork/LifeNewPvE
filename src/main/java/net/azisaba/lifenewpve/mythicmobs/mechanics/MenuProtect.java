package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.ProtectionEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuProtect implements ISkillMechanic, ITargetedEntitySkill {

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (abstractEntity == null || !abstractEntity.isPlayer()) return SkillResult.CONDITION_FAILED;
        Player p = BukkitAdapter.adapt(abstractEntity.asPlayer());
        JavaPlugin.getPlugin(LifeNewPvE.class).runSync(()-> {
            p.closeInventory();
            p.openInventory(new ProtectionEnchantment().getInventory());
        });
        return SkillResult.SUCCESS;
    }
}
