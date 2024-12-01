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

public class FakeBlock implements ISkillMechanic, ITargetedLocationSkill {

    private final Material m;

    private final long duration;

    public FakeBlock(@NotNull MythicLineConfig config) {
        this.m = Material.valueOf(config.getPlaceholderString(new String[]{"m", "material", "t", "type"}, "STONE").get().toUpperCase());
        this.duration = Long.parseLong(config.getPlaceholderString(new String[]{"d", "duration", "md"}, "200").get());
    }

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        AbstractEntity ab = skillMetadata.getCaster().getEntity();
        if (!ab.isPlayer()) return SkillResult.SUCCESS;
        Set<AbstractPlayer> set = Set.of(ab.asPlayer());
        AbstractBlock block = new BukkitBlock(m);

        Map<AbstractLocation, AbstractBlock> map = new HashMap<>();
        map.put(abstractLocation, block);
        Schedulers.async().run(()-> {
            MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(new HashSet<>(set), map);
            Schedulers.async().runLater(()-> {
                Map<AbstractLocation, AbstractBlock> map2 = new HashMap<>();
                map2.put(abstractLocation, abstractLocation.getBlock());
                MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(new HashSet<>(set), map2);
            }, duration);
        });
        return SkillResult.SUCCESS;
    }
}
