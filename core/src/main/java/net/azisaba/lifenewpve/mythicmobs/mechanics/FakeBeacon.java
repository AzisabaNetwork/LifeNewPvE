package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractBlock;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitBlock;
import io.lumine.mythic.bukkit.utils.Schedulers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FakeBeacon implements ISkillMechanic, ITargetedLocationSkill {

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        AbstractEntity ab = skillMetadata.getCaster().getEntity();
        if (!ab.isPlayer()) return SkillResult.SUCCESS;

        int delay = 200;
        Set<AbstractPlayer> set = Set.of(ab.asPlayer());

        Schedulers.async().run(() -> {
            sendBlockOperations(set, getBeacon(abstractLocation), new BukkitBlock(Material.BEACON), delay);
            sendBlockOperations(set, getIronBlock(abstractLocation), new BukkitBlock(Material.IRON_BLOCK), delay);
            sendBlockOperations(set, getBeam(abstractLocation), new BukkitBlock(Material.BEDROCK), delay);
        });
        return SkillResult.SUCCESS;
    }

    private void sendBlockOperations(Set<AbstractPlayer> player, @NotNull Set<AbstractLocation> locations, AbstractBlock block, int delay) {
        Map<AbstractLocation, AbstractBlock> map = new HashMap<>();
        for (AbstractLocation loc : locations) {
            map.put(loc, block);
        }
        MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(player, map);
        Schedulers.async().runLater(() -> {
            Map<AbstractLocation, AbstractBlock> map2 = new HashMap<>();
            for (AbstractLocation loc : locations) {
                map2.put(loc, loc.getBlock());
            }
            MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(player, map2);
        }, delay);
    }

    @NotNull
    private Set<AbstractLocation> getIronBlock(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                locations.add(loc.clone().add(x, 0, z));
            }
        }
        return locations;
    }

    public Set<AbstractLocation> getBeacon(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        locations.add(loc.clone().add(0, 1, 0));
        return locations;
    }

    public Set<AbstractLocation> getBeam(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        Location l = BukkitAdapter.adapt(loc);
        for (int y = loc.getBlockY() + 2; y < BukkitAdapter.adapt(loc.getWorld()).getMaxHeight(); y++) {
            Location get = new Location(l.getWorld(), loc.getBlockX(), y, loc.getBlockZ());
            if (get.getBlock().getType().isSolid()) {
                locations.add(BukkitAdapter.adapt(get));
            }
        }
        return locations;
    }
}
