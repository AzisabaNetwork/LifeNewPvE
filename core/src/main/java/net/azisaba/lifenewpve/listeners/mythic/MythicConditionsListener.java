package net.azisaba.lifenewpve.listeners.mythic;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import net.azisaba.lifenewpve.mythicmobs.conditons.*;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class MythicConditionsListener extends MythicListener {

    @EventHandler
    public void onConditions(@NotNull MythicConditionLoadEvent event) {
        registerCondition(event);
    }

    private void registerCondition(@NotNull MythicConditionLoadEvent event) {
        String conditionName = event.getConditionName().toLowerCase();
        switch (conditionName) {
            case "mythicinradius":
                event.register(new MythicInRadius(event.getConfig()));
                break;
            case "fromsurface":
                event.register(new FromSurface(event.getConfig()));
                break;
            case "containregion":
                event.register(new ContainRegion(event.getConfig()));
                break;
            case "hasmana":
                event.register(new HasMana(event.getConfig()));
                break;
            case "worldmatch":
                event.register(new WorldMatch(event.getConfig()));
                break;
            case "canattack":
                event.register(new CanAttack(event.getConfig()));
                break;
            case "weekofday":
                event.register(new WeekOfDay(event.getConfig()));
                break;
            default:
                // 未知の条件には何もしません
                break;
        }
    }
}
