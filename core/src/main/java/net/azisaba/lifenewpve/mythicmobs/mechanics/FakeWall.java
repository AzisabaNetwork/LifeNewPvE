package net.azisaba.lifenewpve.mythicmobs.mechanics;

import io.lumine.mythic.api.adapters.AbstractBlock;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
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

public class FakeWall implements ISkillMechanic, ITargetedLocationSkill {

    private final Material m;

    public FakeWall(@NotNull MythicLineConfig config) {
        this.m = Material.valueOf(config.getPlaceholderString(new String[]{"m", "material", "t", "type"}, "AIR").get().toUpperCase());
    }

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        AbstractEntity ab = skillMetadata.getCaster().getEntity();
        if (!ab.isPlayer()) return SkillResult.SUCCESS;

        Set<AbstractPlayer> set = Set.of(ab.asPlayer());

        Schedulers.async().run(() -> {
            sendBlockOperations(set, getBeacon(abstractLocation), new BukkitBlock(m));
        });
        return SkillResult.SUCCESS;
    }

    private void sendBlockOperations(Set<AbstractPlayer> player, @NotNull Set<AbstractLocation> locations, AbstractBlock block) {
        Map<AbstractLocation, AbstractBlock> map = new HashMap<>();
        for (AbstractLocation loc : locations) {
            map.put(loc, block);
        }
        MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(player, map);
    }

    public Set<AbstractLocation> getBeacon(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        locations.add(loc.clone().add(0, 0, 0));
        return locations;
    }
}
