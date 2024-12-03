package net.azisaba.lifenewpve.mythicmobs.conditons;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractWorld;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class WorldMatch implements ISkillCondition, ILocationCondition {

    private final AbstractWorld world;

    public WorldMatch(@NotNull MythicLineConfig config) {
        World world = Bukkit.getWorld(config.getPlaceholderString(new String[]{"w", "world"}, "world").get());
        if (world == null) {
            world = Bukkit.getWorlds().getFirst();
        }
        this.world = BukkitAdapter.adapt(world);
    }

    @Override
    public boolean check(@NotNull AbstractLocation abstractLocation) {
        return world.getName().equals(abstractLocation.getWorld().getName());
    }
}
