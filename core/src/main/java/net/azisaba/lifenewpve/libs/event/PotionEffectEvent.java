package net.azisaba.lifenewpve.libs.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PotionEffectEvent extends Event {

    private static final HandlerList handler = new HandlerList();

    private final Player player;


    private final String name;

    private final int level;

    private final long seconds;

    private final boolean remove;


    public PotionEffectEvent(Player player, @NotNull String name, int level, long seconds, boolean remove) {
        super(true);
        this.player = player;
        this.name = name;
        this.level = level;
        this.seconds = seconds;
        this.remove = remove;
    }

    public Player getPlayer() {
        return player;
    }


    @NotNull
    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public long getSeconds() {
        return seconds;
    }

    public boolean isRemove() {
        return remove;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handler;
    }
}
