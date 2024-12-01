package net.azisaba.common;

import net.azisaba.api.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class NewPvE {

    @NotNull
    public static Plugin getPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LifeNewPvE");
        if (plugin == null) {
            throw new IllegalStateException("LifeNewPvE is not loaded");
        }
        return plugin;
    }

    public static SchedulerTask getTask() {
        if (getPlugin() instanceof SchedulerTask task) {
            return task;
        } else {
            throw new IllegalStateException("LifeNewPvE is not loaded");
        }
    }
}
