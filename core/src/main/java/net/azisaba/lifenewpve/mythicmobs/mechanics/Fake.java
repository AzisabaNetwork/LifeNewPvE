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
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitBlock;
import io.lumine.mythic.bukkit.utils.Schedulers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Fake implements ISkillMechanic, ITargetedLocationSkill {

    private final Material m;

    private final long duration;

    private final Type type;

    private final PlaceholderString min;
    private final PlaceholderString max;

    public Fake(@NotNull MythicLineConfig config) {
        this.m = Material.valueOf(config.getPlaceholderString(new String[]{"m", "material"}, "BARRIER").get().toUpperCase());
        this.duration = Long.parseLong(config.getPlaceholderString(new String[]{"d", "duration", "md"}, "0").get());
        this.type = Type.valueOf(config.getPlaceholderString(new String[]{"t" ,"type"}, "NONE").get().toUpperCase());
        this.min = config.getPlaceholderString(new String[]{"min"}, "0,0,0");
        this.max = config.getPlaceholderString(new String[]{"max"}, "0,0,0");
    }

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        AbstractEntity ab = skillMetadata.getCaster().getEntity();
        if (!ab.isPlayer()) return SkillResult.SUCCESS;
        Set<AbstractPlayer> set = Set.of(ab.asPlayer());
        World w = BukkitAdapter.adapt(abstractLocation.getWorld());
        typePacket(type, set, abstractLocation, getArea(getLocation(w, max), getLocation(w, min)));
        return SkillResult.SUCCESS;
    }

    public enum Type {
        NONE, TREE, BEACON
    }

    public Location getLocation(World world, @NotNull PlaceholderString string) {
        String[] split = string.get().split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);
        return new Location(world, x, y, z);
    }

    public Set<AbstractLocation> getArea(@NotNull Location min, @NotNull Location max) {
        Set<AbstractLocation> ab = new HashSet<>();
        World w = min.getWorld();
        for (int x = Math.min(min.getBlockX(), max.getBlockX()); x <= Math.max(min.getBlockX(), max.getBlockX()); x++) {
            for (int y = Math.min(min.getBlockY(), max.getBlockY()); y <= Math.max(min.getBlockY(), max.getBlockZ()); y++) {
                for (int z = Math.min(min.getBlockZ(), max.getBlockZ()); z <= Math.max(min.getBlockZ(), max.getBlockZ()); z++) {
                    ab.add(BukkitAdapter.adapt(new Location(w, x, y, z)));
                }
            }
        }
        return ab;
    }

    public void typePacket(@NotNull Type t, Set<AbstractPlayer> set, AbstractLocation abstractLocation, Set<AbstractLocation> area) {
        if (t.equals(Type.NONE)) {
            AbstractBlock block = new BukkitBlock(m);
            placePacket(set, area, block);

            if (duration == 0) return;
            restorePacket(set, area);

        } else if (t.equals(Type.TREE)) {
            Set<AbstractLocation> tree = getTree(abstractLocation);
            Set<AbstractLocation> leaves = getLeavesTree(abstractLocation);
            AbstractBlock log = new BukkitBlock(Material.OAK_LOG);
            AbstractBlock leave = new BukkitBlock(Material.OAK_LEAVES);

            placePacket(set, leaves, leave);
            placePacket(set, tree, log);

            if (duration == 0) return;
            restorePacket(set, leaves);
            restorePacket(set, tree);

        } else if (t.equals(Type.BEACON)) {
            Set<AbstractLocation> beacon = getBeacon(abstractLocation);
            Set<AbstractLocation> iron = getIronBlock(abstractLocation);
            Set<AbstractLocation> beam = getBeam(abstractLocation);
            AbstractBlock b_beacon = new BukkitBlock(Material.BEACON);
            AbstractBlock b_iron= new BukkitBlock(Material.IRON_BLOCK);
            AbstractBlock b_beam = new BukkitBlock(Material.BEDROCK);

            placePacket(set, beacon, b_beacon);
            placePacket(set, iron, b_iron);
            placePacket(set, beam, b_beam);
            if (duration == 0) return;
            restorePacket(set, beacon);
            restorePacket(set, iron);
            restorePacket(set, beam);
        }
    }

    private void placePacket(Set<AbstractPlayer> set, @NotNull Set<AbstractLocation> locations, AbstractBlock block) {
        Schedulers.async().run(()->{
            Map<AbstractLocation, AbstractBlock> map = new HashMap<>();
            for (AbstractLocation location : locations) {
                map.put(location, block);
            }
            MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(set, map);
        });
    }

    private void restorePacket(Set<AbstractPlayer> set, Set<AbstractLocation> locations) {
        Schedulers.async().runLater(()-> {
            Map<AbstractLocation, AbstractBlock> map = new HashMap<>();
            for (AbstractLocation location : locations) {
                map.put(location, location.getBlock());
            }
            MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(new HashSet<>(set), map);
        }, duration);
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

    @NotNull
    private Set<AbstractLocation> getBeacon(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        locations.add(loc.clone().add(0, 1, 0));
        return locations;
    }

    @NotNull
    private Set<AbstractLocation> getBeam(@NotNull AbstractLocation loc) {
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

    @NotNull
    private Set<AbstractLocation> getTree(@NotNull AbstractLocation loc) {
        Set<AbstractLocation> locations = new HashSet<>();
        locations.add(loc.clone().add(0, 0, 0));
        locations.add( loc.clone().add(0, 1, 0));
        locations.add(loc.clone().add(0, 2, 0));
        locations.add(loc.clone().add(0, 3, 0));
        return locations;
    }

    public Set<AbstractLocation> getLeavesTree(AbstractLocation loc) {
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
