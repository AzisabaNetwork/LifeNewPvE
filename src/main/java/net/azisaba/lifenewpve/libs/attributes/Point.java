package net.azisaba.lifenewpve.libs.attributes;

import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record Point(String world, Set<String> tags, String unique, Location loc) {

    @NotNull
    public String getTagPath() {
        return getRootPath() + "." + getTagValue();
    }

    @NotNull
    public String getTaskPath() {
        return getRootPath() + "." + getTaskValue();
    }

    @NotNull
    public String getLocationPath() {
        return getRootPath() + "." + getLocationValue();
    }

    @NotNull
    @Contract(pure = true)
    public String getRootPath() {
        return "SavePoint." + world + "."  + unique;
    }

    @NotNull
    @Contract(pure = true)
    private String getTaskValue() {
        return "Task";
    }

    @NotNull
    @Contract(pure = true)
    private String getTagValue() {
        return "Tags";
    }

    @NotNull
    @Contract(pure = true)
    private String getLocationValue() {
        return "Location";
    }
}
