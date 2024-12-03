package net.azisaba.lifenewpve.mana;

import net.azisaba.api.mana.IManaBase;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.mana.event.ManaModifiedEvent;
import net.azisaba.lifenewpve.mana.event.ManaModifyEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import net.azisaba.api.utils.ItemPDC;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class ManaBase implements IManaBase, ItemPDC {

    protected final Player player;

    protected final LifeNewPvE plugin;

    private final String mana_value = "net/azisaba/api/mana";

    private final String max_mana_value = "max_mana";

    protected final NumberFormat num = NumberFormat.getNumberInstance();

    protected final PersistentDataContainer pc;

    public ManaBase(@NotNull Player player, LifeNewPvE plugin) {
        this.player = player;
        this.pc = player.getPersistentDataContainer();
        this.plugin = plugin;
        num.setMaximumFractionDigits(1);
    }

    public double getMana() {
        String string = pc.get(plugin.getKey().getOrCreate(mana_value), PersistentDataType.STRING);
        return string == null ? 0 : Double.parseDouble(string);
    }

    @Override
    public double getMaxMana() {
        String string = pc.get(plugin.getKey().getOrCreate(max_mana_value), PersistentDataType.STRING);
        return string == null ? 500 + ManaUtil.getItemMana(player, "max_mana") : Double.parseDouble(string) + ManaUtil.getItemMana(player, "max_mana");
    }

    @Override
    public void setMana(double mana) {
        plugin.runSync(()-> {
            ManaModifyEvent event = new ManaModifyEvent(player, getMana(), mana, getMaxMana());
            if (event.callEvent()) {
                pc.set(LifeNewPvE.getInstance().getKey().getOrCreate(mana_value), PersistentDataType.STRING, String.valueOf(event.getAdd()));
                new ManaModifiedEvent(player, getMana()).callEvent();
            }
        });
    }

    @Override
    public void setMaxMana(double mana) {
        pc.set(LifeNewPvE.getInstance().getKey().getOrCreate(max_mana_value), PersistentDataType.STRING, String.valueOf(mana));
    }

    @Override
    public boolean isManaFull() {
        return getMana() >= getMaxMana();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public static Map<String, String> getManaPlaceholderMap() {
        Map<String, String> map = new HashMap<>();
        map.put("net/azisaba/api/mana", "§d");
        return map;
    }

    @Override
    public Object getAllPDC(NamespacedKey key, PersistentDataType<String, String> type) {
        StringBuilder combinedStrings = new StringBuilder();
        Object combinedObjects = null;

        for (EquipmentSlot slot : getSlots()) {
            String data = getPDC(slot, key, type);
            try {
                int i = Integer.parseInt(data);
                combinedObjects = combinedObjects == null ? i : (int) combinedObjects + i;
            } catch (Exception e) {
                if (data != null) {
                    combinedStrings.append(data);
                    combinedObjects = combinedStrings.toString();
                }
            }
        }
        return combinedObjects;
    }

    @Override
    public String getPDC(EquipmentSlot slot, NamespacedKey key, PersistentDataType<String, String> type) {
        ItemStack item = getItemStack(slot);
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, type);
    }

    public String getPDC(@NotNull ItemStack item, NamespacedKey key, PersistentDataType<String, String> type) {
        return item.getItemMeta().getPersistentDataContainer().get(key, type);
    }

    @Override
    public void setPDC(EquipmentSlot slot, NamespacedKey key, PersistentDataType<String, String> type, String value) {
        ItemStack item = getItemStack(slot);
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, type, value);
        item.setItemMeta(meta);
    }

    @Override
    public ItemStack getItemStack(EquipmentSlot slot) {
        return player.getInventory().getItem(slot);
    }


}
