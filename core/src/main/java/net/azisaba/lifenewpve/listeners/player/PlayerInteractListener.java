package net.azisaba.lifenewpve.listeners.player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.database.DBLootChest;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.utils.CoolTime;
import net.azisaba.lifenewpve.utils.key.LifeKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerInteractListener extends PlayerListener {

    private final LifeNewPvE plugin;

    public PlayerInteractListener(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    private static final Set<UUID> tempCancelled = new HashSet<>();

    private static final Multimap<Class<?>, UUID> ct = HashMultimap.create();

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        Player player = e.getPlayer();
        if (CoolTime.isCoolTime(getClass(), player.getUniqueId(), ct)) return;
        CoolTime.setCoolTime(getClass(), player.getUniqueId(), ct, 20);
        VectorTask.applyVelocityAndRunTask(player, player, VectorTask.getVector(block, player));

    }

    @EventHandler
    public void onInteractFireWork(@NotNull PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null) return;
        if (item.getType() == Material.FIREWORK_ROCKET) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractChest(@NotNull PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.getWorld().getName().contains("open_field")) return;
        if (e.getAction().isLeftClick()) return;
        if (player.isSneaking()) return;
        if (e.getHand() == null || e.getHand() == EquipmentSlot.OFF_HAND) return;

        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;

        e.setCancelled(true);
        if (tempCancelled.contains(player.getUniqueId())) {
            unsetTempCancelled(player.getUniqueId(), 5L);
            return;
        }
        tempCancelled.add(player.getUniqueId());
        plugin.runAsync(()-> {
            boolean opened = new DBLootChest().isLooted(player.getUniqueId(), block.getLocation());
            if (opened) {
                deny(player);
                return;
            }

            plugin.runSync(()-> {
                Chest chest = (Chest) block.getState();
                if (hasChestReward(chest) == null) return;

                giveChestReward(player, chest);
            });
        });
        unsetTempCancelled(player.getUniqueId(), 10L);
    }

    private void unsetTempCancelled(@NotNull UUID uuid, long delay) {
        plugin.runAsyncDelayed(()-> tempCancelled.remove(uuid), delay);
    }

    @Nullable
    private String hasChestReward(@NotNull Chest chest) {
        NamespacedKey key = new LifeKey(plugin).getOrCreate("loot_chest");
        if (!chest.getPersistentDataContainer().has(key)) return null;

        return chest.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private void giveChestReward(Player player, @NotNull Chest chest) {
        for (ItemStack item : chest.getBlockInventory().getContents()) {
            if (item == null) continue;
            for (ItemStack reward : player.getInventory().addItem(item).values()) {
                player.getWorld().dropItem(player.getLocation(), reward);
            }
            accept(player, item);
        }
        plugin.runAsync(()-> {
            new DBLootChest().setLooted(player.getUniqueId(), chest.getLocation(), true);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            tempCancelled.remove(player.getUniqueId());
        });
    }

    private void deny(@NotNull Player player) {
        player.sendMessage(Component.text("§cそのチェストは既に開封済みです！"));
    }

    private void accept(@NotNull Player player, @NotNull ItemStack item) {
        player.sendMessage(item.displayName().append(Component.text(" §7×" + item.getAmount() + "§fを獲得しました。")));
    }
}
