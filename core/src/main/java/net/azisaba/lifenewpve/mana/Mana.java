package net.azisaba.lifenewpve.mana;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import net.azisaba.api.utils.tasks.Task;

public class Mana extends ManaBase implements Task {

    private final BukkitTask task;

    public Mana(@NotNull Player player, LifeNewPvE plugin, Runnable runnable, long delay, long repeat) {
        super(player, plugin);
        this.task = plugin.runAsyncTimer(runnable, delay, repeat);
    }

    @Override
    public void stop() {
        task.cancel();
    }

    @Override
    public BukkitTask getTask() {
        return task;
    }
}
