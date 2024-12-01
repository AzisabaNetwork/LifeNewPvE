package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractBlock;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitBlock;
import io.lumine.mythic.bukkit.utils.Schedulers;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FakeTree implements ISkillMechanic, ITargetedLocationSkill {

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        AbstractEntity ab = skillMetadata.getCaster().getEntity();
        if (!ab.isPlayer()) return SkillResult.SUCCESS;

        int delay = 100;
        Set<AbstractPlayer> set = Set.of(ab.asPlayer());

        Schedulers.async().run(() -> {
            sendBlockOperations(set, getSingleLeavesTree(abstractLocation), new BukkitBlock(Material.OAK_LEAVES), delay);
            sendBlockOperations(set, getSingleTree(abstractLocation), new BukkitBlock(Material.OAK_WOOD), delay);
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
    private Set<AbstractLocation> getSingleTree(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        locations.add(loc.clone().add(0, 0, 0));
        locations.add( loc.clone().add(0, 1, 0));
        locations.add(loc.clone().add(0, 2, 0));
        locations.add(loc.clone().add(0, 3, 0));
        return locations;
    }

    public Set<AbstractLocation> getSingleLeavesTree(AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {

                if (Math.abs(x) + Math.abs(z) != 4) {
                    locations.add(loc.clone().add(x, 1, z));
                }
                locations.add(loc.clone().add(x, 2, z));
            }
        }

        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                if (Math.abs(x) + Math.abs(z) != 2) {
                    locations.add(loc.clone().add(x, 4, z));
                }
                locations.add(loc.clone().add(x, 3, z));
            }
        }

        return locations;
    }
}
