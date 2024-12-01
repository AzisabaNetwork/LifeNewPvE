package net.azisaba.api.utils.key;

import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.Set;

public interface ILifeKey {

    NamespacedKey getOrCreate(String value);

    boolean containsKey(String value);

    Collection<NamespacedKey> getKeys();

    Set<String> getValues();
}
