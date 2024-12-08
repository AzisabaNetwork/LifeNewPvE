package net.azisaba.lifenewpve.listeners.player;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.potion.LifePotion;
import net.azisaba.lifenewpve.mana.ManaRegen;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinListener extends PlayerListener {

    private final LifeNewPvE plugin;

    public PlayerJoinListener(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.runSyncDelayed(()-> {
            new ManaRegen(p, plugin).autoRegen();
            new LifePotion(plugin, p).init();
        }, 40);
    }
}
