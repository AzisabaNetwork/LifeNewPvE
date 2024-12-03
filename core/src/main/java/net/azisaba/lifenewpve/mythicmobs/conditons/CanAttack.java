package net.azisaba.lifenewpve.mythicmobs.conditons;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class CanAttack implements ISkillCondition, IEntityCondition {

    private final double distance;

    public CanAttack(@NotNull MythicLineConfig config) {
        distance = Double.parseDouble(config.getPlaceholderString(new String[]{"d", "distance", "r", "range"}, "5").get());
    }

    @Override
    public boolean check(@NotNull AbstractEntity abstractEntity) {
        if (!abstractEntity.isPlayer()) return false;
        return getTargetedLocation(abstractEntity.asPlayer());
    }


    private boolean getTargetedLocation(@NotNull AbstractPlayer aPlayer) {
        Player player = (Player)aPlayer.getBukkitEntity();
        return rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), distance, (material) -> material == Material.BARRIER );
    }

    private boolean rayTrace(Location start, @NotNull Vector direction, double maxDistance, Predicate<Material> blockFilter) {
        if (!(direction.lengthSquared() < 1.0E-5) && !(maxDistance <= 1.0E-5)) {
            BlockIterator bIterator = new BlockIterator(start.getWorld(), start.toVector(), direction, 0.0, (int) Math.ceil(maxDistance));
            Block block;

            while (bIterator.hasNext()) {
                block = bIterator.next();
                if (!block.isEmpty() && !blockFilter.test(block.getType())) {
                    RayTraceResult res = block.rayTrace(start, direction, maxDistance, FluidCollisionMode.ALWAYS);
                    if (res != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
