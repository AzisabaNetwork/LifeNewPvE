package net.azisaba.lifenewpve.listeners.mythic;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import net.azisaba.lifenewpve.mana.ManaUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class MythicDeathListener extends MythicListener {

    @EventHandler
    public void onDeath(@NotNull MythicMobDeathEvent e) {
        if (!(e.getKiller() instanceof Player p)) return;
        if (e.getEntity() instanceof Player) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        ManaUtil.addMana(p, ManaUtil.getManaSteal(p));
    }
}
