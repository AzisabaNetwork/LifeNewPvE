package net.azisaba.common;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DamageColor {

    @NotNull
    public static Map<String, String> getColors() {
        Plugin plugin = NewPvE.getPlugin();
        Map<String, String> map = new HashMap<>();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("Colors");
        if (cs == null) return map;
        for (String key : cs.getKeys(false)) {
            addColor(key, plugin.getConfig().getString("Colors." + key));
        }
        return map;
    }

    public static void addColor(String name, String color) {
        getColors().put(name, color);
    }
}
