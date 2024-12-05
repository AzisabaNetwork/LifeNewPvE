package net.azisaba.lifenewpve.listeners.player;

import net.azisaba.lifenewpve.libs.VectorTask;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerInteractListener extends PlayerListener implements VectorTask {

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        Player player = e.getPlayer();
        applyVelocityAndRunTask(player, player, getVector(block, player));

    }

    @EventHandler
    public void onInteractFireWork(@NotNull PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null) return;
        if (item.getType() == Material.FIREWORK_ROCKET) {
            e.setCancelled(true);
        }
    }
}
