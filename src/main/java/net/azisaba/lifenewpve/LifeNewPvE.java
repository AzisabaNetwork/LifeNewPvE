package net.azisaba.lifenewpve;

import com.onarandombox.MultiverseCore.MultiverseCore;
import net.azisaba.lifenewpve.commands.*;
import net.azisaba.lifenewpve.libs.Mana;
import net.azisaba.lifenewpve.listeners.*;
import net.azisaba.loreeditor.api.event.EventBus;
import net.azisaba.loreeditor.api.event.ItemEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class LifeNewPvE extends JavaPlugin implements Task {

    private static final Map<String, String> COLORS = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerListeners();
        registerCommands();

        MythicListener.reloadMythic(20);
        spawnNotification();
        updatePointData();
        updateColors();

        Bukkit.getOnlinePlayers().forEach(p -> new Mana(p).runTaskTimer(JavaPlugin.getPlugin(LifeNewPvE.class), 200, 200));
        registerLore();
    }
    @Override
    public void onDisable() {
        ManaListener.Modify.removeAll();
    }

    private void updatePointData() {
       SavePointCommand.updateTags();
    }

    private void updateColors() {
        ConfigurationSection cs = getConfig().getConfigurationSection("Colors");
        if (cs == null) return;
        for (String key : cs.getKeys(false)) {
            COLORS.put(key, getConfig().getString("Colors." + key));
        }
    }

    public static Map<String, String> getColors() {
        return COLORS;
    }

    private void registerListeners() {
        new MythicListener().initialize(this);
        new MultiverseListener().initialize(this);
        new PlayerListener().initialize(this);
        new EntityListener().initialize(this);
        new WorldListener().initialize(this);
        new EnchantListener().initialize(this);
        new PrepareListener(this).initialize();
        new ChunkListener().initialize(this);
        new ManaListener().initialize(this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("worldregen")).setExecutor(new WorldRegenCommand(this));
        Objects.requireNonNull(getCommand("worldset")).setExecutor(new WorldSetCommand());
        Objects.requireNonNull(getCommand("worldcreate")).setExecutor(new WorldCreateCommand(this));
        Objects.requireNonNull(getCommand("worldteleport")).setExecutor(new WorldTeleportCommand());
        Objects.requireNonNull(getCommand("spawn")).setExecutor(new SpawnCommand(this));
        Objects.requireNonNull(getCommand("setspawn")).setExecutor(new SetSpawnCommand(this));
        Objects.requireNonNull(getCommand("savepoint")).setExecutor(new SavePointCommand(this));
        Objects.requireNonNull(getCommand("mode")).setExecutor(new ModeCommand());
        Objects.requireNonNull(getCommand("setmana")).setExecutor(new SetManaCommand());
    }

    private static final String PREFIX_JP = "§7装備したとき：";
    private static final String SUFFIX_JP = "§bマナ +V";

    private void registerLore() {
        EventBus.INSTANCE.register(this, ItemEvent.class, 0, e -> {
            long mana = Mana.getLoreStack(e.getBukkitItem());
            if (mana == -1) return;

            String manaMessage = SUFFIX_JP.replace("V", String.valueOf(mana));
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(PREFIX_JP));
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(manaMessage));
        });
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
                   MythicListener.reloadMythic(100);
               }
           }, 20);
        }
    }

    private void spawnNotification() {
        Component comp = Component.text("コマンド「/spawn」をすることでサーバーのスポーン地点へとテレポートできます。").clickEvent(ClickEvent.suggestCommand("/spawn"));
        runAsyncTimer(()-> {
            for (World world : Bukkit.getWorlds()) {
                if (!world.getName().contains("resource")) continue;
                for (Player player : world.getPlayers()) {
                    if (WorldTeleportCommand.isTeleporting(player)) {
                        player.sendMessage(comp);
                        WorldTeleportCommand.clearTeleporting(player);
                    }
                }
            }
        }, 36000, 36000);
    }
}
