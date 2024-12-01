package net.azisaba.api;

import org.bukkit.scheduler.BukkitTask;

public interface SchedulerTask {

    BukkitTask runAsyncTimer(Runnable runnable, long delay, long loop);

    BukkitTask runSyncTimer(Runnable runnable, long delay, long loop);

    BukkitTask runAsyncDelayed(Runnable runnable, long delay);

    BukkitTask runSyncDelayed(Runnable runnable, long delay);

    BukkitTask runSync(Runnable runnable);

    BukkitTask runAsync(Runnable runnable);
}
