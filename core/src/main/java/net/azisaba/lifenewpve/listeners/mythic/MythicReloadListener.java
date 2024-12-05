package net.azisaba.lifenewpve.listeners.mythic;

import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import net.azisaba.lifenewpve.mythicmobs.Placeholder;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class MythicReloadListener extends MythicListener {

    @EventHandler
    public void onReload(@NotNull MythicReloadedEvent e) {
        new Placeholder(e.getInstance().getPlaceholderManager()).init();
    }
}
