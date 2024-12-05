package net.azisaba.lifenewpve.listeners.mythic;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MythicListener implements Listener {

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        registerAllEvents(pm, plugin);
    }

    private void registerAllEvents(@NotNull PluginManager pm, LifeNewPvE plugin) {
        pm.registerEvents(new MythicConditionsListener(), plugin);
        pm.registerEvents(new MythicMechanicsListener(plugin), plugin);
        pm.registerEvents(new MythicDamageListener(plugin), plugin);
        pm.registerEvents(new MythicReloadListener(), plugin);
        pm.registerEvents(new MythicItemGenerateListener(), plugin);
        pm.registerEvents(new MythicDeathListener(), plugin);
    }

    public static boolean isMythic() {
        return isPluginEnabled();
    }

    private static boolean isPluginEnabled() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return plugin != null && plugin.isEnabled();
    }

    public static void reloadMythic(long delay) {
        notifyPlayers();
        executeReloadCommand(delay);
    }

    private static void executeReloadCommand(long delay) {
        JavaPlugin.getPlugin(LifeNewPvE.class)
                .runSyncDelayed(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a"), delay);
    }

    public static void notifyPlayers() {
        Bukkit.getOnlinePlayers()
                .forEach(p -> p.sendMessage(Component.text("§a§lMythicMobsのリロードが行われます。")));
    }

}
