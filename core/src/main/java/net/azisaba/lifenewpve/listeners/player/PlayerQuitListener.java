package net.azisaba.lifenewpve.listeners.player;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.commands.WorldTeleportCommand;
import net.azisaba.lifenewpve.mana.ManaRegen;
import net.azisaba.lifenewpve.mana.listener.ManaListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener extends PlayerListener {

    private final LifeNewPvE plugin;

    public PlayerQuitListener(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        WorldTeleportCommand.clearTeleporting(p);
        ManaListener.Modify.removeBossBar(p.getUniqueId());
        new ManaRegen(p, plugin).stopRegen();
    }
}
