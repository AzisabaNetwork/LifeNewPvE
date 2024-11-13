package net.azisaba.lifenewpve.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.numbers.RangedInt;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class FromSurface implements ISkillCondition, IEntityCondition {

    private final RangedInt range;
    public FromSurface(@NotNull MythicLineConfig config) {
        range = new RangedInt(config.getPlaceholderString(new String[]{"r", "range"}, "1").get());
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        Entity entity = BukkitAdapter.adapt(abstractEntity);
        Block b = entity.getLocation().getBlock();
        int i = 0;
        while (check(b)) {
            b = b.getRelative(BlockFace.DOWN);
            i++;
            if (b.getY() <= -62) break;
        }
        return range.equals(i);
    }

    private boolean check(@NotNull Block b) {
        Material m = b.getType();
        return (m.isAir()) || (!m.isSolid() && m != Material.WATER && m != Material.LAVA);
    }
}
