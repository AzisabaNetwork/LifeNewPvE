package net.azisaba.lifenewpve.listeners.block;

import io.lumine.mythic.bukkit.MythicBukkit;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.utils.key.LifeKey;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BlockPlaceListener extends BlockListener {

    private final LifeNewPvE plugin;

    public BlockPlaceListener(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)) return;
        if (!p.getWorld().getName().contains("open_field")) return;
        Block b = e.getBlockPlaced();
        if (b.getType() != Material.CHEST) return;

        ItemStack hand = e.getItemInHand();
        if (!hand.hasItemMeta()) return;

        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(hand);
        if (mmid == null) return;
        if (!mmid.contains("ルートチェスト")) return;

        Chest chest = (Chest) b.getState();
        chest.getPersistentDataContainer().set(new LifeKey(plugin).getOrCreate("loot_chest"), PersistentDataType.STRING, "true");
        chest.update();
    }
}
