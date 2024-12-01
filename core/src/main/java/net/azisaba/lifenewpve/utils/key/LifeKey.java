package net.azisaba.lifenewpve.utils.key;

import net.azisaba.api.utils.key.ILifeKey;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class LifeKey implements ILifeKey {

    private final JavaPlugin plugin;

    private static final Map<String, NamespacedKey> keys = new HashMap<>();

    public LifeKey(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public NamespacedKey getOrCreate(String value) {
        if (keys.containsKey(value)) {
            return keys.get(value);
        } else {
            NamespacedKey key = new NamespacedKey(plugin, value);
            keys.put(value, key);
            return key;
        }
    }

    @Override
    public boolean containsKey(String value) {
        return keys.containsKey(value);
    }

    @Override
    public Collection<NamespacedKey> getKeys() {
        return keys.values();
    }

    @Override
    public Set<String> getValues() {
        return keys.keySet();
    }
}
