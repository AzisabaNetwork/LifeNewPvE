package net.azisaba.lifenewpve;

import com.onarandombox.MultiverseCore.MultiverseCore;
import net.azisaba.lifenewpve.commands.*;
import net.azisaba.lifenewpve.listeners.MultiverseListener;
import net.azisaba.lifenewpve.listeners.MythicListener;
import net.azisaba.lifenewpve.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LifeNewPvE extends JavaPlugin implements Task {

    @Override
    public void onEnable() {

        registerListeners();
        registerCommands();
    }

    private void registerListeners() {
        new MythicListener().initialize(this);
        new MultiverseListener().initialize(this);
        new PlayerListener().initialize(this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("worldregen")).setExecutor(new WorldRegenCommand(this));
        Objects.requireNonNull(getCommand("worldcreate")).setExecutor(new WorldCreateCommand(this));
        Objects.requireNonNull(getCommand("worldteleport")).setExecutor(new WorldTeleportCommand());
        Objects.requireNonNull(getCommand("spawn")).setExecutor(new SpawnCommand(this));
        Objects.requireNonNull(getCommand("setspawn")).setExecutor(new SetSpawnCommand(this));
    }

    @Override
    public void runAsync(Runnable runnable) {Bukkit.getScheduler().runTaskAsynchronously(this, runnable);}

    @Override
    public void runSync(Runnable runnable) {Bukkit.getScheduler().runTask(this, runnable);}

    @Override
    public void runSyncDelayed(Runnable runnable, long delay) {Bukkit.getScheduler().runTaskLater(this, runnable, delay);}

    @Override
    public void runAsyncDelayed(Runnable runnable, long delay) {Bukkit.getScheduler().runTaskLaterAsynchronously(this, runnable, delay);}

    @Override
    public void runSyncTimer(Runnable runnable, long delay, long loop) {Bukkit.getScheduler().runTaskTimer(this, runnable, delay, loop);}

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long loop) {Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, loop);}

    public void createWorld(String name, String gen, Difficulty dif, WorldType type, World.Environment environment, String seed, boolean generate) {
        MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
        if (core.getMVWorldManager()
                .addWorld(name, environment, seed, type, generate, gen)) {

           runSyncDelayed(()-> {
               World w = core.getMVWorldManager().getMVWorld(name).getCBWorld();
               if (w == null) return;
               MultiverseListener.settings(w, dif);

               if (MythicListener.isMythic()) {
                   MythicListener.reloadMythic(20);
               }
           }, 80);
        }
    }
}
