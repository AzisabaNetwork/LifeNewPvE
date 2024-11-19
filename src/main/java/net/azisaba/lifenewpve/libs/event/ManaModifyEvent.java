package net.azisaba.lifenewpve.libs.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ManaModifyEvent extends Event implements Cancellable {

    private static final HandlerList handler = new HandlerList();

    private boolean cancelled = false;

    private final Player player;

    private final long before;

    private long add;

    public ManaModifyEvent(Player player, long before, long add) {
        this.player = player;
        this.before = before;
        this.add = add;
    }

    public Player getPlayer() {
        return player;
    }

    public long getBefore() {
        return before;
    }

    public long getAdd() {
        return add;
    }

    public void setAdd(long add) {
        this.add = add;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handler;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
