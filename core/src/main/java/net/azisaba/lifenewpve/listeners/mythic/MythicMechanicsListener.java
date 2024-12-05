package net.azisaba.lifenewpve.listeners.mythic;

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.mythicmobs.mechanics.*;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class MythicMechanicsListener extends MythicListener {

    private final LifeNewPvE plugin;

    public MythicMechanicsListener(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMechanics(@NotNull MythicMechanicLoadEvent event) {
        registerMechanic(event);
    }

    private void registerMechanic(@NotNull MythicMechanicLoadEvent event) {
        String mechanicName = event.getMechanicName().toLowerCase();
        switch (mechanicName) {
            case "setfalldistance":
                event.register(new SetFallDistance(event.getConfig()));
                break;
            case "raidboss":
                event.register(new RaidBoss(plugin));
            case "modifymana":
                event.register(new ModifyMana(event.getConfig()));
                break;
            case "menuprotect":
                event.register(new MenuProtect());
                break;
            case "addlifepotion":
                event.register(new AddLifePotion(plugin, event.getConfig()));
                break;
            case "setscale":
                event.register(new SetScale(plugin, event.getConfig()));
                break;
            case "fake":
                event.register(new Fake(event.getConfig()));
                break;
            default:
                // 未知の条件には何もしません
                break;
        }
    }
}
