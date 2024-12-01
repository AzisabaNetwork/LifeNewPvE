package net.azisaba.api.utils.tasks;

import org.bukkit.scheduler.BukkitTask;

public interface Task {

    void stop();

    BukkitTask getTask();
}
