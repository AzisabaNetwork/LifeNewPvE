package net.azisaba.lifenewpve.listeners.player;

import net.azisaba.lifenewpve.listeners.mythic.MythicListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandListener extends PlayerListener {

    @EventHandler(ignoreCancelled = true)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("lifenewpve.reload.mythicmobs")) return;
        if (e.getMessage().contains("mm r") || e.getMessage().contains("mythicmobs r")) {
            MythicListener.notifyPlayers();
        }
    }
}
