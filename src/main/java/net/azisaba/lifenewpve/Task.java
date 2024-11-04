package net.azisaba.lifenewpve;

public interface Task {

    void runAsyncTimer(Runnable runnable, long delay, long loop);

    void runSyncTimer(Runnable runnable, long delay, long loop);

    void runAsyncDelayed(Runnable runnable, long delay);

    void runSyncDelayed(Runnable runnable, long delay);

    void runSync(Runnable runnable);

    void runAsync(Runnable runnable);
}
