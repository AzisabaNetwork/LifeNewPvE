package net.azisaba.lifenewpve.mythicmobs;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ContainRegion implements ISkillCondition, IEntityCondition {

    private final String region;


    public ContainRegion(@NotNull MythicLineConfig config) {
        region = PlaceholderString.of(config.getString(new String[]{"r", "region"}, "__GLOBAL__")).get().toLowerCase();
    }

    @Override
    public boolean check(@NotNull AbstractEntity abstractEntity) {
        Entity entity = abstractEntity.getBukkitEntity();
        World cb = entity.getWorld();
        com.sk89q.worldedit.world.World we = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(cb);
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(we);
        if (rm != null) {
            Location loc = entity.getLocation();
            ProtectedRegion rg = rm.getApplicableRegions(new BlockVector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                    .getRegions().stream().filter(r -> r.getId().toLowerCase().contains(region)).findFirst().orElse(null);
            return rg != null;
        }
        return false;
    }
}
