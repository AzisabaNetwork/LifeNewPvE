package net.azisaba.api.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public interface ItemPDC {

    void setPDC(EquipmentSlot slot, NamespacedKey key, PersistentDataType<String, String> type, String value);

    String getPDC(EquipmentSlot slot, NamespacedKey key, PersistentDataType<String, String> type);

    ItemStack getItemStack(EquipmentSlot slot);

    default Set<EquipmentSlot> getSlots() {
        Set<EquipmentSlot> set = new HashSet<>();
        set.add(EquipmentSlot.HAND);
        set.add(EquipmentSlot.CHEST);
        set.add(EquipmentSlot.FEET);
        set.add(EquipmentSlot.HEAD);
        set.add(EquipmentSlot.LEGS);
        return set;

    }

    @Nullable Object getAllPDC(NamespacedKey key, PersistentDataType<String, String> type);
}
