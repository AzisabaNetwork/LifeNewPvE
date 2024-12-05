package net.azisaba.lifenewpve.listeners.mythic;

import io.lumine.mythic.bukkit.events.MythicMobItemGenerateEvent;
import io.lumine.mythic.core.items.MythicItem;
import net.minecraft.core.component.DataComponents;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MythicItemGenerateListener extends MythicListener {

    @EventHandler
    public void onGen(@NotNull MythicMobItemGenerateEvent event) {
        handleItemGen(event);
    }

    private void handleItemGen(@NotNull MythicMobItemGenerateEvent event) {
        MythicItem mi = event.getItem();
        String itemGroup = mi != null ? mi.getGroup() : null;
        if (!"Main-Weapon".equals(itemGroup)) return;

        ItemStack item = event.getItemStack();
        if (item != null && item.hasItemMeta()) {
            net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
            nms.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            event.setItemStack(CraftItemStack.asBukkitCopy(nms));
        }
    }
}
