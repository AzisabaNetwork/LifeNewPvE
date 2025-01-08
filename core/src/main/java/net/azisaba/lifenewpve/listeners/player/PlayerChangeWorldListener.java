package net.azisaba.lifenewpve.listeners.player;

import net.azisaba.lifenewpve.listeners.mv.MultiverseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChangeWorldListener extends PlayerListener {

    @EventHandler
    public void onWorldChange(@NotNull PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        //リセット中ワールドに入れないようにする。
        if (MultiverseListener.isResetWorld(p.getWorld().getName())) {
            p.teleport(e.getFrom().getSpawnLocation());
        }
    }
}
