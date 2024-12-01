package net.azisaba.lifenewpve.utils;

import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CoolTime {

    public static boolean isCoolTime(Class<?> clazz, UUID uuid, @NotNull Multimap<Class<?>, UUID> multimap) {
        return multimap.containsEntry(clazz, uuid);
    }

    public static void setCoolTime(Class<?> clazz, UUID uuid, @NotNull Multimap<Class<?>, UUID> multimap, long tick) {
        multimap.put(clazz, uuid);
        JavaPlugin.getPlugin(LifeNewPvE.class).runAsyncDelayed(() -> multimap.remove(clazz, uuid), tick);
    }
}
